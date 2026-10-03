package br.ufpr.sept.so2.modules.egresso.application.ports

import java.util.UUID

/** Nome do aluno e sigla do curso para montar a lista da secretaria (F5.10). */
interface EgressoDiretorioPort {
    fun alunosPorId(ids: Collection<UUID>): Map<UUID, Aluno>

    fun cursosPorId(ids: Collection<UUID>): Map<UUID, Curso>

    data class Aluno(
        val id: UUID,
        val nome: String,
        val grr: String?,
    )

    data class Curso(
        val id: UUID,
        val nome: String,
        val sigla: String,
    )
}
