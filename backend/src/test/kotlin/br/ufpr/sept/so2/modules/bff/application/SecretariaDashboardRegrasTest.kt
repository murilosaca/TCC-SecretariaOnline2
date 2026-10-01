package br.ufpr.sept.so2.modules.bff.application

import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.UUID

class SecretariaDashboardRegrasTest : StringSpec({
    "sem curso vinculado recusa antes de consultar" {
        shouldThrow<AcessoNegadoException> {
            SecretariaDashboardRegras.exigirCursos(emptySet())
        }.message shouldBe SecretariaDashboardRegras.MSG_SEM_CURSO
        val curso = UUID.fromString("01999999-0000-7000-8000-00000000f521")
        SecretariaDashboardRegras.exigirCursos(setOf(curso)) shouldBe setOf(curso)
    }

    "slaStatus distingue breach, janela de 24h e prazo folgado" {
        val agora = OffsetDateTime.parse("2026-10-01T15:00:00Z")
        SecretariaDashboardRegras.slaStatus(agora.minusMinutes(1), agora) shouldBe "danger"
        SecretariaDashboardRegras.slaStatus(agora.plusHours(23), agora) shouldBe "warning"
        SecretariaDashboardRegras.slaStatus(agora.plusHours(25), agora) shouldBe null
        SecretariaDashboardRegras.slaStatus(null, agora) shouldBe null
    }
})
