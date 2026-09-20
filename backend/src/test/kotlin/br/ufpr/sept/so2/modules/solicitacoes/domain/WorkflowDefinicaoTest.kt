package br.ufpr.sept.so2.modules.solicitacoes.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class WorkflowDefinicaoTest : StringSpec({
    "alias REQUEST_ADJUSTMENT resolve para REQUEST_ADJUST do seed" {
        val workflow = workflow()
        workflow.resolverAcao("EM_ANALISE", "REQUEST_ADJUSTMENT") shouldBe "REQUEST_ADJUST"
        workflow.transicionar("EM_ANALISE", "REQUEST_ADJUSTMENT") shouldBe "EM_AJUSTE"
        workflow.estadosComAcoesDeliberativas() shouldBe setOf("EM_ANALISE")
        workflow.acoesDeliberativasDe("EM_ANALISE") shouldBe setOf("DEFER", "INDEFER", "REQUEST_ADJUST")
        workflow.acoesDeliberativasDe("DELIBERADA") shouldBe emptySet()
    }

    "acao em branco e transicao ilegal sao rejeitadas" {
        val workflow = workflow()
        shouldThrow<DadoInvalidoException> { workflow.resolverAcao("EM_ANALISE", "  ") }
        shouldThrow<ConflitoEstadoException> { workflow.transicionar("EM_ANALISE", "CLOSE") }
        workflow.transicionar("EM_ANALISE", "defer") shouldBe "DELIBERADA"
        workflow.acoesDeliberativasDe("CONCLUIDA").shouldBe(emptySet())
        workflow.estadosComAcoesDeliberativas().shouldNotBe(setOf("DELIBERADA"))
    }
})

private fun workflow(): WorkflowDefinicao =
    WorkflowDefinicao(
        "EM_ANALISE",
        mapOf(
            "EM_ANALISE" to mapOf(
                "DEFER" to "DELIBERADA",
                "INDEFER" to "INDEFERIDA",
                "REQUEST_ADJUST" to "EM_AJUSTE",
            ),
            "EM_AJUSTE" to mapOf("RESUBMIT" to "EM_ANALISE"),
            "DELIBERADA" to mapOf("CLOSE" to "CONCLUIDA"),
            "INDEFERIDA" to emptyMap(),
            "CONCLUIDA" to emptyMap(),
        ),
    )
