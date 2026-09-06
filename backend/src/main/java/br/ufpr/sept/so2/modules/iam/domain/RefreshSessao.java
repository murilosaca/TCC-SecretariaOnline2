package br.ufpr.sept.so2.modules.iam.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RefreshSessao {

    private final UUID id;
    private final UUID usuarioId;
    private final String tokenHash;
    private final OffsetDateTime expiresAt;
    private boolean used;
    private boolean revoked;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public RefreshSessao(
            UUID id,
            UUID usuarioId,
            String tokenHash,
            OffsetDateTime expiresAt,
            boolean used,
            boolean revoked,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.used = used;
        this.revoked = revoked;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean expirada(OffsetDateTime agora) {
        return !expiresAt.isAfter(agora);
    }

    public boolean reutilizadaOuRevogada() {
        return used || revoked;
    }

    public void marcarUsada(OffsetDateTime agora) {
        this.used = true;
        this.updatedAt = agora;
    }

    public void revogar(OffsetDateTime agora) {
        this.revoked = true;
        this.updatedAt = agora;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
