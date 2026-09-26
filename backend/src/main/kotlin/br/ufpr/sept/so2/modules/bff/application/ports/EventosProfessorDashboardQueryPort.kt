package br.ufpr.sept.so2.modules.bff.application.ports

import java.time.OffsetDateTime
import java.util.UUID

fun interface EventosProfessorDashboardQueryPort {
    fun consultar(anfitriaoId: UUID, agora: OffsetDateTime): EventosProfessorDashboard

    data class EventosProfessorDashboard(
        val hoje: Int,
        val meusEventos: List<MeuEventoResumo>,
    )

    data class MeuEventoResumo(
        val id: UUID,
        val titulo: String,
        val inicioEm: OffsetDateTime,
        val fimEm: OffsetDateTime,
        val estado: String,
        val links: Map<String, String>,
    )
}
