package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CursoSecretarioJpaRepository : JpaRepository<CursoSecretarioJpaEntity, CursoSecretarioId> {
    fun findByIdCurso(idCurso: UUID): List<CursoSecretarioJpaEntity>

    fun findByIdCursoIn(cursoIds: Collection<UUID>): List<CursoSecretarioJpaEntity>

    fun findByIdUsuario(idUsuario: UUID): List<CursoSecretarioJpaEntity>

    fun deleteByIdCurso(idCurso: UUID)
}
