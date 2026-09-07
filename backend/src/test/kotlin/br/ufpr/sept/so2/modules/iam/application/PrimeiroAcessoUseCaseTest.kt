package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.domain.SenhaReutilizadaException
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.UUID

class PrimeiroAcessoUseCaseTest : StringSpec({
    lateinit var usuarios: IamFakes.Usuarios
    lateinit var outbox: IamFakes.Outbox
    lateinit var audit: IamFakes.Audit
    lateinit var useCase: PrimeiroAcessoUseCase
    lateinit var novo: Usuario

    beforeEach {
        usuarios = IamFakes.Usuarios()
        outbox = IamFakes.Outbox()
        audit = IamFakes.Audit()
        useCase = PrimeiroAcessoUseCase(
            usuarios,
            IamFakes.Historico(),
            IamFakes.Hasher(),
            audit,
            outbox,
        )
        val agora = OffsetDateTime.parse("2026-01-01T00:00:00Z")
        novo = Usuario(
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
            listOf("dashboard.view_own"),
            agora,
            agora,
        )
        usuarios.save(novo)
    }

    "conclui com senha forte e LGPD" {
        useCase.execute(novo.id, "OutraSenhaForte1!", true, "127.0.0.1", "Vitest")
        val persistido = usuarios.findById(novo.id).orElseThrow()
        persistido.precisaPrimeiroAcesso() shouldBe false
        persistido.senhaAlterada shouldBe true
        audit.tipos shouldContain "iam.first_access_completed"
        outbox.tipos shouldContain "iam.first_access_completed"
    }

    "recusa sem LGPD, senha fraca ou temporária" {
        shouldThrow<DadoInvalidoException> {
            useCase.execute(novo.id, "OutraSenhaForte1!", false, "127.0.0.1", "ua")
        }
        shouldThrow<DadoInvalidoException> {
            useCase.execute(novo.id, "fraca", true, "127.0.0.1", "ua")
        }
        shouldThrow<SenhaReutilizadaException> {
            useCase.execute(novo.id, "SenhaTemp1!xx", true, "127.0.0.1", "ua")
        }
    }

    "recusa quando já concluído" {
        useCase.execute(novo.id, "OutraSenhaForte1!", true, "127.0.0.1", "ua")
        shouldThrow<ConflitoEstadoException> {
            useCase.execute(novo.id, "TerceiraSenha1!", true, "127.0.0.1", "ua")
        }
    }
})
