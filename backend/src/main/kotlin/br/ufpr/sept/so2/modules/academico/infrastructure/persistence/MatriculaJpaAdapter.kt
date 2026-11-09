package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.application.ports.MatriculaRepository
import br.ufpr.sept.so2.modules.academico.domain.Matricula
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MatriculaJpaAdapter(
    private val jpaRepository: MatriculaJpaRepository,
) : MatriculaRepository {
    override fun save(matricula: Matricula): Matricula =
        jpaRepository.save(MatriculaJpaEntity.fromDomain(matricula)).toDomain()

    override fun exists(idAluno: UUID, idDisciplina: UUID, idPeriodoLetivo: UUID): Boolean =
        jpaRepository.existsByIdAlunoAndIdDisciplinaAndIdPeriodoLetivo(idAluno, idDisciplina, idPeriodoLetivo)
}
