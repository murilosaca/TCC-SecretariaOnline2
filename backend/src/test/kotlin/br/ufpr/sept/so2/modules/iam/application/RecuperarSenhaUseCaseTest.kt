package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.OffsetDateTime
import java.util.UUID

class RecuperarSenhaUseCaseTest : StringSpec({
    lateinit var outbox: IamFakes.Outbox
    lateinit var audit: IamFakes.Audit
    lateinit var useCase: RecuperarSenhaUseCase

    beforeEach {
        val usuarios = IamFakes.Usuarios()
        outbox = IamFakes.Outbox()
        audit = IamFakes.Audit()
        val agora = OffsetDateTime.now()
        usuarios.save(
            Usuario(
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
                emptyList(),
                agora,
                agora,
            ),
        )
        useCase = RecuperarSenhaUseCase(
            usuarios,
            IamFakes.Jwt(),
            outbox,
            audit,
            IamFakes.Settings(),
        )
    }

    "e-mail cadastrado enfileira outbox" {
        useCase.execute("aluno.dev@ufpr.br", "127.0.0.1")
        outbox.tipos.size shouldBe 1
        outbox.tipos.first() shouldBe "PASSWORD_RESET"
        outbox.payloads.first() shouldContain "/nova-senha?token="
        audit.tipos.contains("iam.password_reset_requested") shouldBe true
    }

    "e-mail inexistente não cria outbox mas audita" {
        useCase.execute("sumido@ufpr.br", "127.0.0.1")
        outbox.tipos.isEmpty() shouldBe true
        audit.tipos.contains("iam.password_reset_requested") shouldBe true
    }
})
