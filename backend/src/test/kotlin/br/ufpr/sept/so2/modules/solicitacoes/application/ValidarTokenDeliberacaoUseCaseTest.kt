package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.IamFakes
import br.ufpr.sept.so2.shared.domain.exception.TokenAcaoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class ValidarTokenDeliberacaoUseCaseTest : StringSpec({
    val jwt = IamFakes.Jwt()
    val jtis = IamFakes.Jtis()
    val useCase = ValidarTokenDeliberacaoUseCase(jwt, jtis)
    val professorId = UUID.fromString("01800000-0000-7000-8000-0000000000dd")
    val solicitacaoId = UUID.fromString("01800000-0000-7000-8000-0000000000aa")

    "token ausente não valida" {
        useCase.execute(null, solicitacaoId, professorId) shouldBe null
        useCase.execute("  ", solicitacaoId, professorId) shouldBe null
    }

    "JTI gasto ou sub divergente vira 401" {
        val token = "delib:$professorId:$solicitacaoId"
        jtis.add("jti-delib-1", java.time.OffsetDateTime.now().plusHours(1))
        shouldThrow<TokenAcaoInvalidoException> {
            useCase.execute(token, solicitacaoId, professorId)
        }
        jtis.itens.clear()
        shouldThrow<TokenAcaoInvalidoException> {
            useCase.execute(token, solicitacaoId, UUID.randomUUID())
        }
        val claims = useCase.execute(token, solicitacaoId, professorId)
        claims?.jti shouldBe "jti-delib-1"
    }
})
