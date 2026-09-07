package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_token")
class RefreshTokenJpaEntity : BaseEntity() {
    @Column(name = "usuario_id", nullable = false)
    var usuarioId: UUID? = null

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    var tokenHash: String? = null

    @Column(name = "expires_at", nullable = false)
    var expiresAt: OffsetDateTime? = null

    @Column(nullable = false)
    var used: Boolean = false

    @Column(nullable = false)
    var revoked: Boolean = false

    fun merge(sessao: RefreshSessao) {
        usuarioId = sessao.usuarioId
        tokenHash = sessao.tokenHash
        expiresAt = sessao.expiresAt
        used = sessao.used
        revoked = sessao.revoked
    }

    fun toDomain(): RefreshSessao =
        RefreshSessao(
            id!!,
            usuarioId!!,
            tokenHash!!,
            expiresAt!!,
            used,
            revoked,
            createdAt!!,
            updatedAt!!,
        )

    companion object {
        fun fromDomain(sessao: RefreshSessao): RefreshTokenJpaEntity {
            val entity = RefreshTokenJpaEntity()
            entity.id = sessao.id
            entity.merge(sessao)
            return entity
        }
    }
}
