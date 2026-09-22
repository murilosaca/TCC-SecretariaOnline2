package br.ufpr.sept.so2.modules.estagio.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CoeMembroJpaRepository : JpaRepository<CoeMembroJpaEntity, CoeMembroId> {
    fun findByIdUsuario(idUsuario: UUID): List<CoeMembroJpaEntity>

    fun findByIdCursoIn(cursoIds: Collection<UUID>): List<CoeMembroJpaEntity>

    fun existsByIdCursoAndIdUsuario(idCurso: UUID, idUsuario: UUID): Boolean
}
