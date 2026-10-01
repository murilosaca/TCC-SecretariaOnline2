package br.ufpr.sept.so2.modules.bff.application.ports

import java.time.OffsetDateTime
import java.util.UUID

fun interface SecretariaFilaDashboardQueryPort {
    fun consultar(atorId: UUID, agora: OffsetDateTime): FilaSecretaria

    data class FilaSecretaria(
        val abertas: Int,
        val atrasadas: Int,
        val concluidasHoje: Int,
        val itens: List<Item>,
    )

    data class Item(
        val id: UUID,
        val protocolo: String,
        val tipoNome: String,
        val estado: String,
        val prazoEm: OffsetDateTime?,
        val href: String,
    )
}
