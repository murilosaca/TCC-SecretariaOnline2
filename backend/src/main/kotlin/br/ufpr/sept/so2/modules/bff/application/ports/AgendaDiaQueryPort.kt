package br.ufpr.sept.so2.modules.bff.application.ports

import java.time.OffsetDateTime
import java.util.UUID

fun interface AgendaDiaQueryPort {
    fun consultar(agora: OffsetDateTime): AgendaDia

    data class AgendaDia(
        val total: Int,
        val itens: List<Item>,
    )

    data class Item(
        val id: UUID,
        val titulo: String,
        val inicioEm: OffsetDateTime,
        val fimEm: OffsetDateTime,
        val estado: String,
    )
}
