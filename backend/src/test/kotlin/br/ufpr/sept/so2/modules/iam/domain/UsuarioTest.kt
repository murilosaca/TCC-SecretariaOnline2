package br.ufpr.sept.so2.modules.iam.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.UUID

class UsuarioTest : StringSpec({
    "bloqueia após dez falhas e libera quando expira" {
        val usuario = usuario(false)
        val agora = OffsetDateTime.parse("2026-03-01T12:00:00Z")
        var bloqueou = false
        repeat(10) {
            bloqueou = usuario.registrarFalha(agora, 10, 15)
        }
        bloqueou shouldBe true
        usuario.estaBloqueado(agora.plusMinutes(14)) shouldBe true
        usuario.liberarBloqueioSeExpirado(agora.plusMinutes(15))
        usuario.estaBloqueado(agora.plusMinutes(15)) shouldBe false
        usuario.falhasConsecutivas shouldBe 0
    }

    "primeiro acesso exige senha não alterada e registra LGPD" {
        val usuario = usuario(false)
        val agora = OffsetDateTime.parse("2026-03-01T12:00:00Z")
        usuario.precisaPrimeiroAcesso() shouldBe true
        usuario.completarPrimeiroAcesso("novo-hash", agora, "127.0.0.1", "Mozilla")
        usuario.precisaPrimeiroAcesso() shouldBe false
        usuario.senhaAlterada shouldBe true
        usuario.lgpdAceiteEm shouldBe agora
        shouldThrow<ConflitoEstadoException> {
            usuario.completarPrimeiroAcesso("outro", agora, "127.0.0.1", "Mozilla")
        }
    }
})

private fun usuario(senhaAlterada: Boolean): Usuario {
    val agora = OffsetDateTime.parse("2026-01-01T00:00:00Z")
    return Usuario(
        UUID.fromString("01800000-0000-7000-8000-0000000000aa"),
        Email.of("aluno.dev@ufpr.br"),
        null,
        Grr.of("GRR20240001"),
        "hash-temp",
        senhaAlterada,
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
}
