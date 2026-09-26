package br.ufpr.sept.so2.modules.bff.infrastructure

import br.ufpr.sept.so2.modules.bff.application.ports.DeliberacaoDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.DeliberacaoDashboardQueryPort.DeliberacaoDashboard
import br.ufpr.sept.so2.modules.bff.application.ports.DeliberacaoDashboardQueryPort.FilaItemResumo
import br.ufpr.sept.so2.modules.solicitacoes.application.ListarFilaDeliberacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Component
class DeliberacaoDashboardQueryAdapter(
    private val listarFilaDeliberacaoUseCase: ListarFilaDeliberacaoUseCase,
) : DeliberacaoDashboardQueryPort {

    @Transactional(readOnly = true)
    override fun consultar(): DeliberacaoDashboard {
        val agora = OffsetDateTime.now()
        val limiteUrgente = agora.plusHours(HORAS_URGENTE)
        val page = listarFilaDeliberacaoUseCase.execute(
            null,
            false,
            PageRequest.of(0, JANELA_KPI, Sort.by(Sort.Direction.ASC, "prazoEm")),
        )
        val slaUrgentes = page.content.count { item ->
            item.prazoEm != null && item.prazoEm!!.isBefore(limiteUrgente)
        }
        val fila = page.content.take(FILA_LIMITE).map { toResumo(it, agora, limiteUrgente) }
        return DeliberacaoDashboard(page.totalElements.toInt(), slaUrgentes, fila)
    }

    companion object {
        private const val FILA_LIMITE = 5
        private const val JANELA_KPI = 100
        private const val HORAS_URGENTE = 24L

        private fun toResumo(
            solicitacao: Solicitacao,
            agora: OffsetDateTime,
            limiteUrgente: OffsetDateTime,
        ): FilaItemResumo {
            val prazo = solicitacao.prazoEm
            val urgente = prazo != null && prazo.isBefore(limiteUrgente)
            return FilaItemResumo(
                solicitacao.id,
                solicitacao.protocolo.valor,
                solicitacao.tipoNome,
                solicitacao.estado,
                prazo,
                solicitacao.prazoVencido(agora),
                urgente,
                "/solicitacoes/${solicitacao.id}",
            )
        }
    }
}
