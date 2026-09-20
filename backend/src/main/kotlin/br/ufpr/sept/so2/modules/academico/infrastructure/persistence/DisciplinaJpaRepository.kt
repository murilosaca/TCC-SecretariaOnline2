package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface DisciplinaJpaRepository : JpaRepository<DisciplinaJpaEntity, UUID>, JpaSpecificationExecutor<DisciplinaJpaEntity> {
    fun findByIdCurso(idCurso: UUID, pageable: Pageable): Page<DisciplinaJpaEntity>

    fun findByIdCursoIn(cursoIds: Collection<UUID>, pageable: Pageable): Page<DisciplinaJpaEntity>

    fun existsByIdCursoAndCodigoIgnoreCase(idCurso: UUID, codigo: String): Boolean
}
