package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao;
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
public class RefreshTokenJpaEntity extends BaseEntity {

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    @Column(nullable = false)
    private boolean revoked;

    protected RefreshTokenJpaEntity() {
    }

    public static RefreshTokenJpaEntity fromDomain(RefreshSessao sessao) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(sessao.getId());
        entity.merge(sessao);
        return entity;
    }

    public void merge(RefreshSessao sessao) {
        this.usuarioId = sessao.getUsuarioId();
        this.tokenHash = sessao.getTokenHash();
        this.expiresAt = sessao.getExpiresAt();
        this.used = sessao.isUsed();
        this.revoked = sessao.isRevoked();
    }

    public RefreshSessao toDomain() {
        return new RefreshSessao(
                getId(),
                usuarioId,
                tokenHash,
                expiresAt,
                used,
                revoked,
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
