package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import jakarta.persistence.LockModeType
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

interface OutboxEventJpaRepository : JpaRepository<OutboxEventJpaEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEventJpaEntity e where e.id = :id")
    fun findByIdForUpdate(@Param("id") id: UUID): Optional<OutboxEventJpaEntity>

    @Query(
        """
        select e from OutboxEventJpaEntity e
        where (:status is null or e.status = :status)
          and (:tipo is null or e.tipo = :tipo)
        """,
    )
    fun filtrar(
        @Param("status") status: String?,
        @Param("tipo") tipo: String?,
        pageable: Pageable,
    ): Page<OutboxEventJpaEntity>

    @Query("select min(e.createdAt) from OutboxEventJpaEntity e where e.status = 'PENDING'")
    fun menorPending(): OffsetDateTime?
}
