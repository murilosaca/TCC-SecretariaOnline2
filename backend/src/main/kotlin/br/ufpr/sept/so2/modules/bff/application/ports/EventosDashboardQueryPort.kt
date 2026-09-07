package br.ufpr.sept.so2.modules.bff.application.ports

import java.time.OffsetDateTime
import java.util.UUID

fun interface EventosDashboardQueryPort {
    fun consultar(agora: OffsetDateTime): EventosDashboard

    data class EventosDashboard(val hoje: Int, val proximos: List<EventoResumo>)

    data class EventoResumo(
        val id: UUID,
        val titulo: String,
        val inicioEm: OffsetDateTime,
        val fimEm: OffsetDateTime,
        val janelaAtiva: Boolean,
    )
}
