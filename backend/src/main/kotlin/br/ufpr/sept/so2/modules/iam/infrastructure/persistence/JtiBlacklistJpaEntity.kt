package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "jti_blacklist")
class JtiBlacklistJpaEntity(
    @Id
    @Column(length = 64, nullable = false)
    var jti: String? = null,
    @Column(name = "expires_at", nullable = false)
    var expiresAt: OffsetDateTime? = null,
    @Column(name = "consumed_at", nullable = false)
    var consumedAt: OffsetDateTime? = null,
)
