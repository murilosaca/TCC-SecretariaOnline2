package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.exception.CredenciaisInvalidasException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import java.time.OffsetDateTime
import java.util.UUID

class RefreshTokenUseCaseTest : StringSpec({
    lateinit var tokens: IamFakes.RefreshTokens
    lateinit var audit: IamFakes.Audit
    lateinit var useCase: RefreshTokenUseCase
    lateinit var raw: String

    beforeEach {
        tokens = IamFakes.RefreshTokens()
        audit = IamFakes.Audit()
        val usuarios = IamFakes.Usuarios()
        val agora = OffsetDateTime.now()
        val usuario = Usuario(
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
            listOf("dashboard.view_own"),
            agora,
            agora,
        )
        usuarios.save(usuario)
        raw = "refresh-opaco-1"
        tokens.save(
            RefreshSessao(
                Uuids.v7(),
                usuario.id,
                "th:$raw",
                agora.plusDays(7),
                false,
                false,
                agora,
                agora,
            ),
        )
        useCase = RefreshTokenUseCase(
            tokens,
            usuarios,
            IamFakes.Jwt(),
            IamFakes.TokenHasher(),
            audit,
            IamFakes.Settings(),
        )
    }

    "rotaciona refresh válido" {
        val result = useCase.execute(raw, "127.0.0.1")
        result.accessToken shouldStartWith "access:"
        result.refreshToken shouldNotBe raw
    }

    "reuse revoga todas as sessões" {
        useCase.execute(raw, "127.0.0.1")
        shouldThrow<CredenciaisInvalidasException> { useCase.execute(raw, "127.0.0.1") }
        audit.tipos shouldContain "iam.suspicious_token_reuse"
        tokens.byHash.values.all { it.revoked } shouldBe true
    }
})
