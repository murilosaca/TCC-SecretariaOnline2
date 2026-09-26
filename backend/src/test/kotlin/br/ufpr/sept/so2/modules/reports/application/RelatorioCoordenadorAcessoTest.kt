package br.ufpr.sept.so2.modules.reports.application

import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class RelatorioCoordenadorAcessoTest : StringSpec({
    "exige a capability report.view_coordinator" {
        shouldThrow<AcessoNegadoException> {
            RelatorioCoordenadorAcesso.exigirCap(listOf("course.config"))
        }
        RelatorioCoordenadorAcesso.exigirCap(listOf("report.view_coordinator"))
    }

    "exige curso do escopo do coordenador" {
        val meu = UUID.fromString("01999999-0000-7000-8000-00000000c061")
        val outro = UUID.fromString("01999999-0000-7000-8000-00000000c062")
        RelatorioCoordenadorAcesso.exigirDono(meu, setOf(meu)) shouldBe meu
        shouldThrow<AcessoNegadoException> {
            RelatorioCoordenadorAcesso.exigirDono(outro, setOf(meu))
        }
        shouldThrow<AcessoNegadoException> {
            RelatorioCoordenadorAcesso.exigirDono(null, emptySet())
        }
        RelatorioCoordenadorAcesso.exigirDono(null, setOf(meu)) shouldBe meu
    }
})
