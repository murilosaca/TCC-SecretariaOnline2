package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface DisciplinaJpaRepository : JpaRepository<DisciplinaJpaEntity, UUID>, JpaSpecificationExecutor<DisciplinaJpaEntity> {
    fun findByIdCurso(idCurso: UUID, pageable: Pageable): Page<DisciplinaJpaEntity>

    fun findByIdCursoIn(cursoIds: Collection<UUID>, pageable: Pageable): Page<DisciplinaJpaEntity>

    fun existsByIdCursoAndCodigoIgnoreCase(idCurso: UUID, codigo: String): Boolean

    fun findFirstByIdCursoAndCodigoIgnoreCase(idCurso: UUID, codigo: String): java.util.Optional<DisciplinaJpaEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DisciplinaJpaEntity d where d.id in :ids order by d.id")
    fun lockByIds(@Param("ids") ids: Collection<UUID>): List<DisciplinaJpaEntity>
}
