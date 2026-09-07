package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DisciplinaJpaRepository : JpaRepository<DisciplinaJpaEntity, UUID> {
    fun findByIdCurso(idCurso: UUID, pageable: Pageable): Page<DisciplinaJpaEntity>

    fun existsByIdCursoAndCodigoIgnoreCase(idCurso: UUID, codigo: String): Boolean
}
