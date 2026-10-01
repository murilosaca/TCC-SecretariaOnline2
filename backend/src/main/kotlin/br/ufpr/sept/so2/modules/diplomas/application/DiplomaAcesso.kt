package br.ufpr.sept.so2.modules.diplomas.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import java.util.UUID

/**
 * Escopo de secretaria em diploma. Mensagens duplicadas de propósito:
 * `diplomas` não importa `reports.application`.
 * Revalidado no use case — não confiar só em claim JWT.
 */
internal object DiplomaAcesso {
    const val MSG_FORA_ESCOPO: String = "Curso fora do escopo da sua secretaria."
    const val MSG_SEM_CURSO: String = "Nenhum curso vinculado à sua secretaria foi encontrado."
    const val MSG_NAO_ENCONTRADO: String = "Diploma não encontrado."

    fun cursos(port: CursoEscopoPort, atorId: UUID): Set<UUID> = port.cursoIdsDoUsuario(atorId)

    fun exigirEscopo(vinculados: Set<UUID>) {
        if (vinculados.isEmpty()) {
            throw AcessoNegadoException(MSG_SEM_CURSO)
        }
    }

    fun exigirCursoNoEscopo(cursoId: UUID, vinculados: Set<UUID>): UUID {
        exigirEscopo(vinculados)
        if (cursoId !in vinculados) {
            throw AcessoNegadoException(MSG_FORA_ESCOPO)
        }
        return cursoId
    }

    fun exigirDiploma(cursos: Set<UUID>, repository: DiplomaRepository, diplomaId: UUID): Diploma {
        val diploma = repository.findById(diplomaId)
            ?: throw RecursoNaoEncontradoException(MSG_NAO_ENCONTRADO)
        if (diploma.idCurso !in cursos) {
            throw RecursoNaoEncontradoException(MSG_NAO_ENCONTRADO)
        }
        return diploma
    }
}
