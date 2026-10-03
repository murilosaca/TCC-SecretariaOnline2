package br.ufpr.sept.so2.modules.comunicacao.domain

import java.time.OffsetDateTime
import java.util.UUID

class Comunicacao(
    val id: UUID,
    val tipo: TipoComunicacao,
    val titulo: String,
    val corpo: String,
    val prioridade: PrioridadeComunicacao,
    val autorId: UUID,
    val audienciaTipo: TipoAudiencia,
    val audienciaId: UUID,
    val expiresAt: OffsetDateTime?,
    val createdAt: OffsetDateTime,
) {
    fun expirada(agora: OffsetDateTime): Boolean =
        expiresAt != null && !expiresAt.isAfter(agora)
}
