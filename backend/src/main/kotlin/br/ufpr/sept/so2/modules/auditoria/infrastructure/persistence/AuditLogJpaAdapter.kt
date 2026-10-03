package br.ufpr.sept.so2.modules.auditoria.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class AuditLogJpaAdapter(
    private val jpaRepository: AuditLogJpaRepository,
) : AuditLogPort {
    override fun append(tipo: String, atorId: UUID?, payload: String?, ip: String?) {
        val corpo = payload ?: ""
        jpaRepository.save(
            AuditLogJpaEntity(
                id = Uuids.v7(),
                tipo = tipo,
                atorId = atorId,
                payload = corpo,
                ip = ip,
                createdAt = OffsetDateTime.now(),
                payloadAntes = null,
                payloadDepois = corpo,
            ),
        )
    }
}
