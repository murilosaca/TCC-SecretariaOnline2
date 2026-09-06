package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "senha_historico")
public class SenhaHistoricoJpaEntity {

    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected SenhaHistoricoJpaEntity() {
    }

    public SenhaHistoricoJpaEntity(UUID id, UUID usuarioId, String senhaHash, OffsetDateTime createdAt) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.senhaHash = senhaHash;
        this.createdAt = createdAt;
    }

    public String getSenhaHash() {
        return senhaHash;
    }
}
