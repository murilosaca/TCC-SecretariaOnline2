package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MatriculaJpaRepository : JpaRepository<MatriculaJpaEntity, UUID> {
    fun existsByIdAlunoAndIdDisciplinaAndIdPeriodoLetivo(
        idAluno: UUID,
        idDisciplina: UUID,
        idPeriodoLetivo: UUID,
    ): Boolean
}
