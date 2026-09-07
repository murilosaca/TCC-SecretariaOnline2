package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.exception.CredenciaisInvalidasException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import java.time.OffsetDateTime
import java.util.UUID

class LoginUseCaseTest : StringSpec({
    lateinit var usuarios: IamFakes.Usuarios
    lateinit var hasher: IamFakes.Hasher
    lateinit var audit: IamFakes.Audit
    lateinit var useCase: LoginUseCase
    lateinit var aluno: Usuario

    beforeEach {
        usuarios = IamFakes.Usuarios()
        hasher = IamFakes.Hasher()
        audit = IamFakes.Audit()
        useCase = LoginUseCase(
            usuarios,
            IamFakes.RefreshTokens(),
            hasher,
            IamFakes.Jwt(),
            IamFakes.TokenHasher(),
            audit,
            IamFakes.Settings(),
        )
        aluno = usuario(true)
        usuarios.save(aluno)
    }

    "login com e-mail ou GRR emite token" {
        val porEmail = useCase.execute("aluno.dev@ufpr.br", "SenhaForte1!", "127.0.0.1")
        porEmail.mustChangePassword shouldBe false
        porEmail.accessToken shouldStartWith "access:"
        porEmail.expiresIn shouldBe 900

        val porGrr = useCase.execute("GRR20240001", "SenhaForte1!", "127.0.0.1")
        porGrr.mustChangePassword shouldBe false
        audit.tipos shouldContain "iam.login_success"
    }

    "primeiro acesso sinaliza mustChangePassword" {
        val novo = usuario(false)
        usuarios.save(novo)
        val result = useCase.execute("novo.dev@ufpr.br", "SenhaForte1!", "10.0.0.1")
        result.mustChangePassword shouldBe true
    }

    "credenciais inválidas são idênticas e usam hash dummy" {
        shouldThrow<CredenciaisInvalidasException> {
            useCase.execute("sumido@ufpr.br", "errada", "127.0.0.1")
        }
        shouldThrow<CredenciaisInvalidasException> {
            useCase.execute("aluno.dev@ufpr.br", "errada", "127.0.0.1")
        }
        (hasher.dummyCalls >= 1) shouldBe true
        audit.tipos shouldContain "iam.login_failed"
    }

    "décima falha bloqueia conta com evento interno" {
        repeat(10) {
            shouldThrow<CredenciaisInvalidasException> {
                useCase.execute("aluno.dev@ufpr.br", "errada", "127.0.0.1")
            }
        }
        audit.tipos shouldContain "iam.account_blocked"
        usuarios.findById(aluno.id).orElseThrow().estaBloqueado(OffsetDateTime.now()) shouldBe true
    }
})

private fun usuario(senhaAlterada: Boolean): Usuario {
    val agora = OffsetDateTime.parse("2026-01-01T00:00:00Z")
    val email = if (senhaAlterada) "aluno.dev@ufpr.br" else "novo.dev@ufpr.br"
    val grr = if (senhaAlterada) "GRR20240001" else "GRR20240002"
    return Usuario(
        UUID.randomUUID(),
        Email.of(email),
        null,
        Grr.of(grr),
        "h:SenhaForte1!",
        senhaAlterada,
        if (senhaAlterada) agora else null,
        null,
        null,
        true,
        0,
        null,
        listOf("dashboard.view_own"),
        agora,
        agora,
    )
}
