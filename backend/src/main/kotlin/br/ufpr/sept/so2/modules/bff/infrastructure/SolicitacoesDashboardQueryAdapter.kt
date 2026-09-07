package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort.PendenciaResumo
import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort.SolicitacaoResumo
import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort.SolicitacoesDashboard
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Component
class SolicitacoesDashboardQueryAdapter(
    private val solicitacaoRepository: SolicitacaoRepository,
) : SolicitacoesDashboardQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(solicitanteId: UUID): SolicitacoesDashboard {
        val recente = Sort.by(Sort.Direction.DESC, "createdAt")
        val agora = OffsetDateTime.now()
        val ultimas = solicitacaoRepository
            .findMinhas(solicitanteId, null, null, null, PageRequest.of(0, 5, recente))
            .content
            .map { toResumo(it, agora) }
        val pendencias = solicitacaoRepository
            .findMinhas(solicitanteId, ESTADO_PENDENCIA, null, null, PageRequest.of(0, 3, recente))
            .content
            .map(::toPendencia)
        return SolicitacoesDashboard(contarAbertas(solicitanteId), pendencias, ultimas)
    }

    private fun contarAbertas(solicitanteId: UUID): Int =
        ESTADOS_ABERTOS.sumOf { estado ->
            solicitacaoRepository
                .findMinhas(solicitanteId, estado, null, null, PageRequest.of(0, 1))
                .totalElements
                .toInt()
        }

    companion object {
        private val ESTADOS_ABERTOS = setOf("EM_ANALISE", "EM_AJUSTE")
        private const val ESTADO_PENDENCIA = "EM_AJUSTE"

        private fun toResumo(solicitacao: Solicitacao, agora: OffsetDateTime): SolicitacaoResumo =
            SolicitacaoResumo(
                solicitacao.id,
                solicitacao.protocolo.valor,
                solicitacao.tipoNome,
                solicitacao.estado,
                solicitacao.prazoEm,
                solicitacao.prazoVencido(agora),
            )

        private fun toPendencia(solicitacao: Solicitacao): PendenciaResumo =
            PendenciaResumo(
                solicitacao.id,
                solicitacao.tipoNome,
                solicitacao.estado,
                "/solicitacoes/${solicitacao.id}",
            )
    }
}
