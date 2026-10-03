package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository
import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
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

    override fun findByTokenHash(tokenHash: String): Optional<RefreshSessao> =
        jpaRepository.findByTokenHash(tokenHash).map { it.toDomain() }

    override fun findById(id: UUID): Optional<RefreshSessao> =
        jpaRepository.findById(id).map { it.toDomain() }

    override fun findAtivas(usuarioId: UUID, agora: OffsetDateTime): List<RefreshSessao> =
        jpaRepository.findAtivas(usuarioId, agora)
            .map { it.toDomain() }
            .sortedByDescending { it.createdAt }

    @Transactional
    override fun revokeAllByUsuarioId(usuarioId: UUID) {
        jpaRepository.revokeAllByUsuarioId(usuarioId)
    }

    @Transactional
    override fun revokeOthers(usuarioId: UUID, manterId: UUID) {
        jpaRepository.revokeOthers(usuarioId, manterId)
    }
}
