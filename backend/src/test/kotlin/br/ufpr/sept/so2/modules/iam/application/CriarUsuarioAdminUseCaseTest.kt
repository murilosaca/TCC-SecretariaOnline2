package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.domain.PoliticaSenha
import br.ufpr.sept.so2.modules.iam.domain.SenhaTemporaria
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.util.UUID

class CriarUsuarioAdminUseCaseTest : StringSpec({
    "cria usuario com senhaAlterada false, outbox e audit sem expor senha" {
        val usuarios = IamFakes.Usuarios()
        val hasher = IamFakes.Hasher()
        val outbox = IamFakes.Outbox()
        val audit = IamFakes.Audit()
        val useCase = CriarUsuarioAdminUseCase(
            usuarios,
            hasher,
            outbox,
            audit,
            ObjectMapper(),
        )
        val operador = UUID.randomUUID()
        val criado = useCase.execute(
            operador,
            "Maria Silva",
            "maria.nova@ufpr.br",
            null,
            "GRR20248888",
            "127.0.0.1",
        )
        criado.senhaAlterada shouldBe false
        criado.nome shouldBe "Maria Silva"
        criado.senhaHash shouldNotBe null
        outbox.tipos shouldBe listOf("iam.user_created")
        outbox.payloads[0].shouldContain("maria.nova@ufpr.br")
        outbox.payloads[0].contains("senha") shouldBe false
        audit.tipos shouldBe listOf("iam.user_created")
    }

    "senha temporaria atende politica" {
        repeat(20) {
            PoliticaSenha.atende(SenhaTemporaria.gerar()) shouldBe true
        }
    }
})

class ResetSenhaAdminUseCaseTest : StringSpec({
    "enfileira PASSWORD_RESET reusando JWT 1-uso" {
        val usuarios = IamFakes.Usuarios()
        val jwt = IamFakes.Jwt()
        val outbox = IamFakes.Outbox()
        val audit = IamFakes.Audit()
        val agora = java.time.OffsetDateTime.now()
        val alvo = br.ufpr.sept.so2.modules.iam.domain.Usuario(
            UUID.randomUUID(),
            "Alvo",
            br.ufpr.sept.so2.shared.domain.valueobject.Email.of("alvo@ufpr.br"),
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
        )
        usuarios.save(alvo)
        val useCase = ResetSenhaAdminUseCase(
            usuarios,
            jwt,
            outbox,
            audit,
            IamFakes.Settings(),
            ObjectMapper(),
        )
        useCase.execute(UUID.randomUUID(), alvo.id, "127.0.0.1")
        outbox.tipos shouldBe listOf("PASSWORD_RESET")
        outbox.payloads[0].shouldContain("/nova-senha?token=")
        outbox.payloads[0].contains("senha") shouldBe false
        audit.tipos shouldBe listOf("iam.password_reset_admin")
    }
})
