package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLogJpaEntity {

    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String tipo;

    @Column(name = "ator_id")
    private UUID atorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(length = 64)
    private String ip;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected AuditLogJpaEntity() {
    }

    public AuditLogJpaEntity(UUID id, String tipo, UUID atorId, String payload, String ip, OffsetDateTime createdAt) {
        this.id = id;
        this.tipo = tipo;
        this.atorId = atorId;
        this.payload = payload;
        this.ip = ip;
        this.createdAt = createdAt;
    }
}
