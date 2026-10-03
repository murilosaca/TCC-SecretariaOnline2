package br.ufpr.sept.so2.modules.auditoria.application

import java.time.OffsetDateTime
import java.util.UUID

data class AuditLogItem(
    val id: UUID,
    val atorId: UUID?,
    val atorNome: String?,
    val acao: String,
    val entidade: String?,
    val ip: String?,
    val timestamp: OffsetDateTime,
    val payloadAntes: String?,
    val payloadDepois: String?,
)

data class AuditLogPagina(
    val itens: List<AuditLogItem>,
    val numero: Int,
    val tamanho: Int,
    val total: Long,
)
