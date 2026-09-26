package br.ufpr.sept.so2.modules.bff.application.ports

import java.util.UUID

fun interface TccsProfessorDashboardQueryPort {
    fun consultar(revisorId: UUID): TccsProfessorDashboard

    data class TccsProfessorDashboard(
        val total: Int,
        val itens: List<ItemResumo>,
    )

    data class ItemResumo(
        val id: UUID,
        val titulo: String,
        val estado: String,
        val href: String,
    )
}
