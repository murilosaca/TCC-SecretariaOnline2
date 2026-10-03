package br.ufpr.sept.so2.modules.auditoria.api.dto

import br.ufpr.sept.so2.modules.auditoria.application.AuditLogItem
import java.time.OffsetDateTime
import java.util.UUID

data class AuditLogItemResponse(
    val id: UUID,
    val atorId: UUID?,
    val atorNome: String?,
    val acao: String,
    val entidade: String?,
    val ip: String?,
    val timestamp: OffsetDateTime,
    val payloadAntes: String?,
    val payloadDepois: String?,
) {
    companion object {
        fun from(item: AuditLogItem): AuditLogItemResponse =
            AuditLogItemResponse(
                item.id,
                item.atorId,
                item.atorNome,
                item.acao,
                item.entidade,
                item.ip,
                item.timestamp,
                item.payloadAntes,
                item.payloadDepois,
            )
    }
}
