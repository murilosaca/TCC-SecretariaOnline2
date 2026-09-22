package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface CursoJpaRepository : JpaRepository<CursoJpaEntity, UUID> {
    fun existsBySiglaIgnoreCase(sigla: String): Boolean

    fun existsByCodigoIgnoreCase(codigo: String): Boolean

    fun findByCodigoIgnoreCase(codigo: String): Optional<CursoJpaEntity>

    fun findByIdIn(ids: Collection<UUID>, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<CursoJpaEntity>

    fun findByIdCoordenador(idCoordenador: UUID): List<CursoJpaEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select c from CursoJpaEntity c where c.id = :id")
    fun findByIdForUpdate(@Param("id") id: UUID): Optional<CursoJpaEntity>
}
