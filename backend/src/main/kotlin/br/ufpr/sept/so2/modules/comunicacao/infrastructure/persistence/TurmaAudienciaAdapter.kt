package br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence

import br.ufpr.sept.so2.modules.comunicacao.application.ports.TurmaAudienciaPort
import br.ufpr.sept.so2.modules.comunicacao.application.ports.TurmaGravada
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TurmaAudienciaAdapter(
    private val turmaRepository: TurmaJpaRepository,
    private val matriculaRepository: TurmaAlunoJpaRepository,
) : TurmaAudienciaPort {
    override fun salvar(turma: TurmaGravada): TurmaGravada {
        val entity = turmaRepository.findById(turma.id).orElseGet { TurmaJpaEntity() }
        entity.id = turma.id
        entity.cursoId = turma.cursoId
        entity.professorId = turma.professorId
        entity.codigo = turma.codigo
        entity.nome = turma.nome
        val salva = turmaRepository.save(entity)
        return TurmaGravada(
            requireNotNull(salva.id),
            requireNotNull(salva.cursoId),
            requireNotNull(salva.professorId),
            salva.codigo,
            salva.nome,
        )
    }

    override fun findByProfessor(professorId: UUID): List<TurmaGravada> =
        turmaRepository.findByProfessorIdOrderByNomeAsc(professorId).map { it.toGravada() }

    override fun findByIdEProfessor(id: UUID, professorId: UUID): TurmaGravada? =
        turmaRepository.findByIdAndProfessorId(id, professorId).map { it.toGravada() }.orElse(null)

    override fun findByProfessorECodigo(professorId: UUID, codigo: String): TurmaGravada? =
        turmaRepository.findByProfessorIdAndCodigo(professorId, codigo).map { it.toGravada() }.orElse(null)

    override fun alunoIds(turmaId: UUID): List<UUID> =
        matriculaRepository.findByIdTurma(turmaId).mapNotNull { it.idAluno }

    override fun matricularSeAusente(turmaId: UUID, alunoId: UUID) {
        if (matriculaRepository.existsByIdTurmaAndIdAluno(turmaId, alunoId)) {
            return
        }
        matriculaRepository.save(
            TurmaAlunoJpaEntity().apply {
                idTurma = turmaId
                idAluno = alunoId
            },
        )
    }

    private fun TurmaJpaEntity.toGravada() = TurmaGravada(
        requireNotNull(id),
        requireNotNull(cursoId),
        requireNotNull(professorId),
        codigo,
        nome,
    )
}
