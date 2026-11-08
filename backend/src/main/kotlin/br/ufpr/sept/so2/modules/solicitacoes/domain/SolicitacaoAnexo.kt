package br.ufpr.sept.so2.modules.solicitacoes.domain

import java.time.OffsetDateTime
import java.util.UUID

class SolicitacaoAnexo(
    val id: UUID,
    val solicitacaoId: UUID,
    val nome: String,
    val contentType: String,
    val tamanho: Int,
    val sha256: String,
    val storageKey: String,
    val createdAt: OffsetDateTime,
)
