package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.application.ports.JtiBlacklistRepository
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class JtiBlacklistJpaAdapter(
    private val jpaRepository: JtiBlacklistJpaRepository,
) : JtiBlacklistRepository {
    override fun add(jti: String, expiresAt: OffsetDateTime) {
        jpaRepository.save(JtiBlacklistJpaEntity(jti, expiresAt, OffsetDateTime.now()))
    }

    override fun contains(jti: String): Boolean = jpaRepository.existsById(jti)
}
