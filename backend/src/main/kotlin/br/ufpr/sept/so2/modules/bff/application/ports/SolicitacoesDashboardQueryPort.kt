package br.ufpr.sept.so2.modules.bff.application.ports

import java.time.OffsetDateTime
import java.util.UUID

fun interface SolicitacoesDashboardQueryPort {
    fun consultar(solicitanteId: UUID): SolicitacoesDashboard

    data class SolicitacoesDashboard(
        val abertas: Int,
        val pendencias: List<PendenciaResumo>,
        val ultimas: List<SolicitacaoResumo>,
    )

    data class PendenciaResumo(
        val id: UUID,
        val titulo: String,
        val estado: String,
        val href: String,
    )

    data class SolicitacaoResumo(
        val id: UUID,
        val protocolo: String,
        val tipoNome: String,
        val estado: String,
        val prazoEm: OffsetDateTime?,
        val slaVencido: Boolean,
    )
}
