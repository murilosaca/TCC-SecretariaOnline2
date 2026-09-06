package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.exception.CredenciaisInvalidasException;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenUseCaseTest {

    private IamFakes.RefreshTokens tokens;
    private IamFakes.Audit audit;
    private RefreshTokenUseCase useCase;
    private String raw;

    @BeforeEach
    void setUp() {
        tokens = new IamFakes.RefreshTokens();
        audit = new IamFakes.Audit();
        IamFakes.Usuarios usuarios = new IamFakes.Usuarios();
        OffsetDateTime agora = OffsetDateTime.now();
        Usuario usuario = new Usuario(
                UUID.fromString("01800000-0000-7000-8000-0000000000cc"),
                Email.of("aluno.dev@ufpr.br"),
                null,
                null,
                "h:x",
                true,
                agora,
                null,
                null,
                true,
                0,
                null,
                List.of("dashboard.view_own"),
                agora,
                agora
        );
        usuarios.save(usuario);
        raw = "refresh-opaco-1";
        tokens.save(new RefreshSessao(
                Uuids.v7(),
                usuario.getId(),
                "th:" + raw,
                agora.plusDays(7),
                false,
                false,
                agora,
                agora
        ));
        useCase = new RefreshTokenUseCase(
                tokens,
                usuarios,
                new IamFakes.Jwt(),
                new IamFakes.TokenHasher(),
                audit,
                new IamFakes.Settings()
        );
    }

    @Test
    void rotacionaRefreshValido() {
        LoginResult result = useCase.execute(raw, "127.0.0.1");
        assertTrue(result.accessToken().startsWith("access:"));
        assertNotEquals(raw, result.refreshToken());
    }

    @Test
    void reuseRevogaTodasAsSessoes() {
        useCase.execute(raw, "127.0.0.1");
        assertThrows(CredenciaisInvalidasException.class, () -> useCase.execute(raw, "127.0.0.1"));
        assertTrue(audit.tipos.contains("iam.suspicious_token_reuse"));
        assertTrue(tokens.byHash.values().stream().allMatch(RefreshSessao::isRevoked));
    }
}
