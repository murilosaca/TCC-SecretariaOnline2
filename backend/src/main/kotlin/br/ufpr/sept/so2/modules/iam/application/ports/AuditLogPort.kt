package br.ufpr.sept.so2.modules.iam.application.ports

import java.util.UUID

interface AuditLogPort {
    fun append(tipo: String, atorId: UUID?, payload: String?, ip: String?)
}
