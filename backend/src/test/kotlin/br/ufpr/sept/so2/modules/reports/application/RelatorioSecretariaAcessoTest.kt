package br.ufpr.sept.so2.modules.reports.application

import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class RelatorioSecretariaAcessoTest : StringSpec({
    "exige a capability report.view_secretary" {
        shouldThrow<AcessoNegadoException> {
            RelatorioSecretariaAcesso.exigirCap(listOf("report.view_coordinator"))
        }.message shouldBe RelatorioSecretariaAcesso.MSG_SEM_CAP
        RelatorioSecretariaAcesso.exigirCap(listOf("report.view_secretary"))
    }

    "exige ao menos um curso vinculado" {
        shouldThrow<AcessoNegadoException> {
            RelatorioSecretariaAcesso.exigirEscopo(emptySet())
        }.message shouldBe RelatorioSecretariaAcesso.MSG_SEM_CURSO
    }

    "bloqueia curso fora do escopo" {
        val meu = UUID.fromString("01999999-0000-7000-8000-00000000f518")
        val outro = UUID.fromString("01999999-0000-7000-8000-00000000f519")
        shouldThrow<AcessoNegadoException> {
            RelatorioSecretariaAcesso.exigirCursoNoEscopo(outro, setOf(meu))
        }.message shouldBe RelatorioSecretariaAcesso.MSG_FORA_ESCOPO
        RelatorioSecretariaAcesso.exigirCursoNoEscopo(meu, setOf(meu)) shouldBe meu
    }
})
