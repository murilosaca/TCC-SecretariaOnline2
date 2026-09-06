package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "jti_blacklist")
public class JtiBlacklistJpaEntity {

    @Id
    @Column(length = 64, nullable = false)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "consumed_at", nullable = false)
    private OffsetDateTime consumedAt;

    protected JtiBlacklistJpaEntity() {
    }

    public JtiBlacklistJpaEntity(String jti, OffsetDateTime expiresAt, OffsetDateTime consumedAt) {
        this.jti = jti;
        this.expiresAt = expiresAt;
        this.consumedAt = consumedAt;
    }
}
