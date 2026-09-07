package br.ufpr.sept.so2.modules.academico.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class PeriodoLetivoTest : StringSpec({
    "rejeita semestre inválido" {
        shouldThrow<DadoInvalidoException> { periodo(2026, 3, "2026-02-01", "2026-07-15") }
    }

    "rejeita intervalo invertido" {
        shouldThrow<DadoInvalidoException> { periodo(2026, 1, "2026-07-15", "2026-02-01") }
    }

    "detecta sobreposição e período vigente" {
        val primeiro = periodo(2026, 1, "2026-02-01", "2026-07-15")
        val segundo = periodo(2026, 2, "2026-07-01", "2026-12-20")
        val adjacente = periodo(2026, 2, "2026-07-15", "2026-12-20")

        primeiro.sobrepoe(segundo) shouldBe true
        primeiro.sobrepoe(adjacente) shouldBe false
        primeiro.vigenteEm(LocalDate.parse("2026-03-10")) shouldBe true
        primeiro.vigenteEm(LocalDate.parse("2026-08-01")) shouldBe false
    }
})

private fun periodo(ano: Int, semestre: Int, inicio: String, fim: String): PeriodoLetivo {
    val agora = OffsetDateTime.parse("2026-01-01T00:00:00Z")
    return PeriodoLetivo(
        UUID.fromString("01800000-0000-7000-8000-000000000001"),
        ano,
        semestre,
        LocalDate.parse(inicio),
        LocalDate.parse(fim),
        true,
        agora,
        agora,
    )
}
