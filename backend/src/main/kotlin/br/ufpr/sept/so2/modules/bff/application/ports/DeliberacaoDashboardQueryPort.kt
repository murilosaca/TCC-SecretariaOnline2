package br.ufpr.sept.so2.modules.bff.application.ports

import java.time.OffsetDateTime
import java.util.UUID

fun interface DeliberacaoDashboardQueryPort {
    fun consultar(): DeliberacaoDashboard

    data class DeliberacaoDashboard(
        val pendentes: Int,
        val slaUrgentes: Int,
        val fila: List<FilaItemResumo>,
    )

    data class FilaItemResumo(
        val id: UUID,
        val protocolo: String,
        val tipoNome: String,
        val estado: String,
        val prazoEm: OffsetDateTime?,
        val slaVencido: Boolean,
        val urgente: Boolean,
        val href: String,
    )
}
