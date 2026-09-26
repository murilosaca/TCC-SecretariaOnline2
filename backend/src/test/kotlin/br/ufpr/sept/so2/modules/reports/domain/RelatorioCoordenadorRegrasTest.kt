package br.ufpr.sept.so2.modules.reports.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class RelatorioCoordenadorRegrasTest : StringSpec({
    "parsePeriodo aceita AAAA-S" {
        val p = RelatorioCoordenadorRegras.parsePeriodo("2026-2")
        p!!.ano shouldBe 2026
        p.semestre shouldBe 2
        RelatorioCoordenadorRegras.rotulo(p.ano, p.semestre) shouldBe "2026/2"
        RelatorioCoordenadorRegras.codigo(p.ano, p.semestre) shouldBe "2026-2"
    }

    "parsePeriodo nulo retorna null" {
        RelatorioCoordenadorRegras.parsePeriodo(null).shouldBeNull()
        RelatorioCoordenadorRegras.parsePeriodo("  ").shouldBeNull()
    }

    "parsePeriodo rejeita formato inválido" {
        shouldThrow<IllegalArgumentException> {
            RelatorioCoordenadorRegras.parsePeriodo("2026/2")
        }
        shouldThrow<IllegalArgumentException> {
            RelatorioCoordenadorRegras.parsePeriodo("2026-3")
        }
    }
})
