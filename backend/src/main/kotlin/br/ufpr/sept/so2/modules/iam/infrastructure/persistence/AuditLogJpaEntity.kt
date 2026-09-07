package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "audit_log")
class AuditLogJpaEntity(
    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    var id: UUID? = null,
    @Column(nullable = false, length = 80)
    var tipo: String? = null,
    @Column(name = "ator_id")
    var atorId: UUID? = null,
    @Column(nullable = false, columnDefinition = "TEXT")
    var payload: String? = null,
    @Column(length = 64)
    var ip: String? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,
)
