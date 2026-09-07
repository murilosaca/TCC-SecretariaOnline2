package br.ufpr.sept.so2.modules.solicitacoes.domain

import java.time.OffsetDateTime
import java.util.UUID

data class SolicitacaoEvento(
    val id: UUID,
    val tipo: String,
    val estadoDe: String?,
    val estadoPara: String,
    val atorId: UUID?,
    val parecer: String?,
    val payload: String?,
    val createdAt: OffsetDateTime,
)
