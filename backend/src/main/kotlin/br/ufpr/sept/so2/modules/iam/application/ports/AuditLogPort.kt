package br.ufpr.sept.so2.modules.iam.application.ports

import java.util.UUID

interface AuditLogPort {
    fun append(tipo: String, atorId: UUID?, payload: String?, ip: String?)

    fun appendDiff(tipo: String, atorId: UUID?, antes: String?, depois: String?, ip: String?) {
        append(tipo, atorId, depois ?: antes, ip)
    }
}
