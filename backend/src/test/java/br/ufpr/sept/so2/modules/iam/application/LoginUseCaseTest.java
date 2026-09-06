package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.exception.CredenciaisInvalidasException;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.domain.valueobject.Grr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginUseCaseTest {

    private IamFakes.Usuarios usuarios;
    private IamFakes.Hasher hasher;
    private IamFakes.Audit audit;
    private LoginUseCase useCase;
    private Usuario aluno;

    @BeforeEach
    void setUp() {
        usuarios = new IamFakes.Usuarios();
        hasher = new IamFakes.Hasher();
        audit = new IamFakes.Audit();
        useCase = new LoginUseCase(
                usuarios,
                new IamFakes.RefreshTokens(),
                hasher,
                new IamFakes.Jwt(),
                new IamFakes.TokenHasher(),
                audit,
                new IamFakes.Settings()
        );
        aluno = usuario(true);
        usuarios.save(aluno);
    }

    @Test
    void loginComEmailOuGrrEmiteToken() {
        LoginResult porEmail = useCase.execute("aluno.dev@ufpr.br", "SenhaForte1!", "127.0.0.1");
        assertFalse(porEmail.mustChangePassword());
        assertTrue(porEmail.accessToken().startsWith("access:"));
        assertEquals(900, porEmail.expiresIn());

        LoginResult porGrr = useCase.execute("GRR20240001", "SenhaForte1!", "127.0.0.1");
        assertFalse(porGrr.mustChangePassword());
        assertTrue(audit.tipos.contains("iam.login_success"));
    }

    @Test
    void primeiroAcessoSinalizaMustChangePassword() {
        Usuario novo = usuario(false);
        usuarios.save(novo);
        LoginResult result = useCase.execute("novo.dev@ufpr.br", "SenhaForte1!", "10.0.0.1");
        assertTrue(result.mustChangePassword());
    }

    @Test
    void credenciaisInvalidasSaoIdenticasEUsamHashDummy() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> useCase.execute("sumido@ufpr.br", "errada", "127.0.0.1"));
        assertThrows(CredenciaisInvalidasException.class,
                () -> useCase.execute("aluno.dev@ufpr.br", "errada", "127.0.0.1"));
        assertTrue(hasher.dummyCalls >= 1);
        assertTrue(audit.tipos.contains("iam.login_failed"));
    }

    @Test
    void decimaFalhaBloqueiaContaComEventoInterno() {
        for (int i = 0; i < 10; i++) {
            assertThrows(CredenciaisInvalidasException.class,
                    () -> useCase.execute("aluno.dev@ufpr.br", "errada", "127.0.0.1"));
        }
        assertTrue(audit.tipos.contains("iam.account_blocked"));
        assertTrue(usuarios.findById(aluno.getId()).orElseThrow().estaBloqueado(OffsetDateTime.now()));
    }

    private static Usuario usuario(boolean senhaAlterada) {
        OffsetDateTime agora = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        String email = senhaAlterada ? "aluno.dev@ufpr.br" : "novo.dev@ufpr.br";
        String grr = senhaAlterada ? "GRR20240001" : "GRR20240002";
        return new Usuario(
                UUID.randomUUID(),
                Email.of(email),
                null,
                Grr.of(grr),
                "h:SenhaForte1!",
                senhaAlterada,
                senhaAlterada ? agora : null,
                null,
                null,
                true,
                0,
                null,
                List.of("dashboard.view_own"),
                agora,
                agora
        );
    }
}
