package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.domain.SenhaAtualIncorretaException
import br.ufpr.sept.so2.modules.iam.domain.SenhaReutilizadaException
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.UUID

class TrocarSenhaUseCaseTest : StringSpec({
    "senha atual errada não troca o hash nem revoga sessão" {
        val agora = OffsetDateTime.parse("2026-10-01T12:00:00Z")
        val usuarios = IamFakes.Usuarios()
        val tokens = IamFakes.RefreshTokens()
        val usuario = usuario(agora)
        usuarios.save(usuario)
        val atual = Uuids.v7()
        val outra = Uuids.v7()
        tokens.save(sessao(atual, usuario.id, "a", agora))
        tokens.save(sessao(outra, usuario.id, "b", agora))
        val useCase = TrocarSenhaUseCase(
            usuarios,
            IamFakes.Hasher(),
            IamFakes.Historico(),
            tokens,
            IamFakes.Audit(),
        )
        shouldThrow<SenhaAtualIncorretaException> {
            useCase.execute(usuario.id, "errada", "OutraSenhaForte1!", atual, "127.0.0.1")
        }
        usuarios.byId.getValue(usuario.id).senhaHash shouldBe "h:atual"
        tokens.findById(atual).get().revoked shouldBe false
        tokens.findById(outra).get().revoked shouldBe false
    }

    "troca válida derruba a outra sessão e mantém a atual" {
        val agora = OffsetDateTime.parse("2026-10-01T12:00:00Z")
        val usuarios = IamFakes.Usuarios()
        val tokens = IamFakes.RefreshTokens()
        val usuario = usuario(agora)
        usuarios.save(usuario)
        val atual = Uuids.v7()
        val outra = Uuids.v7()
        tokens.save(sessao(atual, usuario.id, "a", agora))
        tokens.save(sessao(outra, usuario.id, "b", agora))
        val useCase = TrocarSenhaUseCase(
            usuarios,
            IamFakes.Hasher(),
            IamFakes.Historico(),
            tokens,
            IamFakes.Audit(),
        )
        useCase.execute(usuario.id, "atual", "OutraSenhaForte1!", atual, "127.0.0.1")
        usuarios.byId.getValue(usuario.id).senhaHash shouldBe "h:OutraSenhaForte1!"
        tokens.findById(atual).get().revoked shouldBe false
        tokens.findById(outra).get().revoked shouldBe true
        shouldThrow<SenhaReutilizadaException> {
            useCase.execute(usuario.id, "OutraSenhaForte1!", "OutraSenhaForte1!", atual, "127.0.0.1")
        }
    }
})

private fun usuario(agora: OffsetDateTime): Usuario =
    Usuario(
        UUID.fromString("01800000-0000-7000-8000-0000000000dd"),
        "Aluno",
        Email.of("aluno.senha@ufpr.br"),
        null,
        Grr.of("GRR20240011"),
        "h:atual",
        true,
        agora,
        null,
        null,
        true,
        0,
        null,
        listOf("user.update_own_profile"),
        agora,
        agora,
    )

private fun sessao(id: UUID, usuarioId: UUID, hash: String, agora: OffsetDateTime) =
    br.ufpr.sept.so2.modules.iam.domain.RefreshSessao(
        id,
        usuarioId,
        hash,
        agora.plusDays(7),
        false,
        false,
        agora,
        agora,
    )
