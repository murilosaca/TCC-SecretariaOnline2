package br.ufpr.sept.so2.modules.comunicacao.application.ports

import java.util.UUID

interface TurmaAudienciaPort {
    fun salvar(turma: TurmaGravada): TurmaGravada

    fun findByProfessor(professorId: UUID): List<TurmaGravada>

    fun findByIdEProfessor(id: UUID, professorId: UUID): TurmaGravada?

    fun findByProfessorECodigo(professorId: UUID, codigo: String): TurmaGravada?

    fun alunoIds(turmaId: UUID): List<UUID>

    fun matricularSeAusente(turmaId: UUID, alunoId: UUID)
}

data class TurmaGravada(
    val id: UUID,
    val cursoId: UUID,
    val professorId: UUID,
    val codigo: String,
    val nome: String,
)
