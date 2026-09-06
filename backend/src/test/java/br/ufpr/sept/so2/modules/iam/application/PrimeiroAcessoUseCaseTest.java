package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.domain.SenhaReutilizadaException;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.domain.valueobject.Grr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimeiroAcessoUseCaseTest {

    private IamFakes.Usuarios usuarios;
    private IamFakes.Outbox outbox;
    private IamFakes.Audit audit;
    private PrimeiroAcessoUseCase useCase;
    private Usuario novo;

    @BeforeEach
    void setUp() {
        usuarios = new IamFakes.Usuarios();
        outbox = new IamFakes.Outbox();
        audit = new IamFakes.Audit();
        useCase = new PrimeiroAcessoUseCase(
                usuarios,
                new IamFakes.Historico(),
                new IamFakes.Hasher(),
                audit,
                outbox
        );
        OffsetDateTime agora = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        novo = new Usuario(
                UUID.fromString("01800000-0000-7000-8000-0000000000bb"),
                Email.of("novo.dev@ufpr.br"),
                null,
                Grr.of("GRR20240002"),
                "h:SenhaTemp1!xx",
                false,
                null,
                null,
                null,
                true,
                0,
                null,
                List.of("dashboard.view_own"),
                agora,
                agora
        );
        usuarios.save(novo);
    }

    @Test
    void concluiComSenhaForteELgpd() {
        useCase.execute(novo.getId(), "OutraSenhaForte1!", true, "127.0.0.1", "Vitest");
        Usuario persistido = usuarios.findById(novo.getId()).orElseThrow();
        assertFalse(persistido.precisaPrimeiroAcesso());
        assertTrue(persistido.isSenhaAlterada());
        assertTrue(audit.tipos.contains("iam.first_access_completed"));
        assertTrue(outbox.tipos.contains("iam.first_access_completed"));
    }

    @Test
    void recusaSemLgpdSenhaFracaOuTemporaria() {
        assertThrows(DadoInvalidoException.class,
                () -> useCase.execute(novo.getId(), "OutraSenhaForte1!", false, "127.0.0.1", "ua"));
        assertThrows(DadoInvalidoException.class,
                () -> useCase.execute(novo.getId(), "fraca", true, "127.0.0.1", "ua"));
        assertThrows(SenhaReutilizadaException.class,
                () -> useCase.execute(novo.getId(), "SenhaTemp1!xx", true, "127.0.0.1", "ua"));
    }

    @Test
    void recusaQuandoJaConcluido() {
        useCase.execute(novo.getId(), "OutraSenhaForte1!", true, "127.0.0.1", "ua");
        assertThrows(ConflitoEstadoException.class,
                () -> useCase.execute(novo.getId(), "TerceiraSenha1!", true, "127.0.0.1", "ua"));
    }
}
