package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

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
        jpaRepository.save(
            AuditLogJpaEntity(
                Uuids.v7(),
                tipo,
                atorId,
                payload ?: "",
                ip,
                OffsetDateTime.now(),
            ),
        )
    }
}
