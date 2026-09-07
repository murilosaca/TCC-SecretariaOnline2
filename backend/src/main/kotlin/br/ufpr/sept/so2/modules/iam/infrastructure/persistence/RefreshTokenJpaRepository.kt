package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenJpaEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RefreshTokenJpaEntity r where r.tokenHash = :hash")
    fun lockByTokenHash(@Param("hash") hash: String): Optional<RefreshTokenJpaEntity>

    @Modifying
    @Query("update RefreshTokenJpaEntity r set r.revoked = true where r.usuarioId = :usuarioId and r.revoked = false")
    fun revokeAllByUsuarioId(@Param("usuarioId") usuarioId: UUID): Int
}
