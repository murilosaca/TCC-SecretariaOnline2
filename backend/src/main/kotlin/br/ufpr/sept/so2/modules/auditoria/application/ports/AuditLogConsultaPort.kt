package br.ufpr.sept.so2.modules.auditoria.application.ports

import br.ufpr.sept.so2.modules.auditoria.application.AuditLogItem
import java.time.OffsetDateTime

interface AuditLogConsultaPort {
    fun contar(ator: String?, acao: String?, de: OffsetDateTime, ateExclusivo: OffsetDateTime): Long

    fun listar(
        ator: String?,
        acao: String?,
        de: OffsetDateTime,
        ateExclusivo: OffsetDateTime,
        offset: Int,
        limite: Int,
    ): List<AuditLogItem>
}
