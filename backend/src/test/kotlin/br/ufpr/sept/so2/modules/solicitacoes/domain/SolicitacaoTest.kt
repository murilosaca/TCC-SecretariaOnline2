package br.ufpr.sept.so2.modules.solicitacoes.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.OffsetDateTime
import java.util.UUID

class SolicitacaoTest : StringSpec({
    "transicao invalida eh rejeitada" {
        val solicitacao = aberta()
        val ex = shouldThrow<ConflitoEstadoException> {
            solicitacao.transicionar(
                UUID.fromString("01800000-0000-7000-8000-0000000000ee"),
                workflow(),
                "CLOSE",
                solicitacao.solicitanteId,
                "não permitido",
                AGORA,
            )
        }
        ex.message shouldContain "EM_ANALISE --CLOSE-->"
        solicitacao.estado shouldBe "EM_ANALISE"
        solicitacao.eventos.size shouldBe 1
    }

    "defer move para deliberada e registra evento" {
        val solicitacao = aberta()
        solicitacao.transicionar(
            UUID.fromString("01800000-0000-7000-8000-0000000000ee"),
            workflow(),
            "DEFER",
            solicitacao.solicitanteId,
            "Deferido.",
            AGORA,
        )
        solicitacao.estado shouldBe "DELIBERADA"
        solicitacao.eventos.size shouldBe 2
        val ultimo = solicitacao.eventos[1]
        ultimo.tipo shouldBe Solicitacao.EVENTO_TRANSICAO
        ultimo.estadoDe shouldBe "EM_ANALISE"
        ultimo.estadoPara shouldBe "DELIBERADA"
    }
})

private val AGORA: OffsetDateTime = OffsetDateTime.parse("2026-03-01T12:00:00Z")

private fun aberta(): Solicitacao {
    val tipo = TipoSolicitacao(
        UUID.fromString("01800000-0000-7000-8000-000000000001"),
        "DECLARACAO_SIMPLES",
        "Declaração simples",
        "teste",
        TipoSolicitacao.PUBLISHED,
        "{}",
        "{}",
        15,
        1,
        AGORA,
        AGORA,
    )
    return Solicitacao.abrir(
        UUID.fromString("01800000-0000-7000-8000-0000000000aa"),
        UUID.fromString("01800000-0000-7000-8000-0000000000bb"),
        tipo,
        UUID.fromString("01800000-0000-7000-8000-0000000000cc"),
        Protocolo.formatar(2026, 1),
        "{\"finalidade\":\"vinculo\"}",
        workflow(),
        AGORA,
    )
}

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
