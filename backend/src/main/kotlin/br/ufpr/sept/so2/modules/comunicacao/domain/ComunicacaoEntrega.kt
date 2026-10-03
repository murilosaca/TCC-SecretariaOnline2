package br.ufpr.sept.so2.modules.comunicacao.domain

import java.time.OffsetDateTime
import java.util.UUID

class ComunicacaoEntrega(
    val id: UUID,
    val comunicacaoId: UUID,
    val destinatarioId: UUID,
    var readAt: OffsetDateTime?,
    val acaoHref: String?,
    var inApp: Boolean,
    var emailEnviado: Boolean,
    val createdAt: OffsetDateTime,
)
