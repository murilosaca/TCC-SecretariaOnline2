package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository
import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID

@Component
class RefreshTokenJpaAdapter(
    private val jpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun save(sessao: RefreshSessao): RefreshSessao {
        val entity = jpaRepository.findById(sessao.id)
            .orElseGet { RefreshTokenJpaEntity.fromDomain(sessao) }
        entity.merge(sessao)
        return jpaRepository.save(entity).toDomain()
    }

    override fun lockByTokenHash(tokenHash: String): Optional<RefreshSessao> =
        jpaRepository.lockByTokenHash(tokenHash).map { it.toDomain() }

    @Transactional
    override fun revokeAllByUsuarioId(usuarioId: UUID) {
        jpaRepository.revokeAllByUsuarioId(usuarioId)
    }
}
