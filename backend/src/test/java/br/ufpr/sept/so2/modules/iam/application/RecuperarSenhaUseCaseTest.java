package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecuperarSenhaUseCaseTest {

    private IamFakes.Outbox outbox;
    private IamFakes.Audit audit;
    private RecuperarSenhaUseCase useCase;

    @BeforeEach
    void setUp() {
        IamFakes.Usuarios usuarios = new IamFakes.Usuarios();
        outbox = new IamFakes.Outbox();
        audit = new IamFakes.Audit();
        OffsetDateTime agora = OffsetDateTime.now();
        usuarios.save(new Usuario(
                UUID.randomUUID(),
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
                List.of(),
                agora,
                agora
        ));
        useCase = new RecuperarSenhaUseCase(
                usuarios,
                new IamFakes.Jwt(),
                outbox,
                audit,
                new IamFakes.Settings()
        );
    }

    @Test
    void emailCadastradoEnfileiraOutbox() {
        useCase.execute("aluno.dev@ufpr.br", "127.0.0.1");
        assertEquals(1, outbox.tipos.size());
        assertEquals("PASSWORD_RESET", outbox.tipos.getFirst());
        assertTrue(outbox.payloads.getFirst().contains("/nova-senha?token="));
        assertTrue(audit.tipos.contains("iam.password_reset_requested"));
    }

    @Test
    void emailInexistenteNaoCriaOutboxMasAudita() {
        useCase.execute("sumido@ufpr.br", "127.0.0.1");
        assertTrue(outbox.tipos.isEmpty());
        assertTrue(audit.tipos.contains("iam.password_reset_requested"));
    }
}
