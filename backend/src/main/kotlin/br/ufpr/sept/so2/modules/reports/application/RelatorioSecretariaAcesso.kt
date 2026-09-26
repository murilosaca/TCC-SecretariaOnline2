package br.ufpr.sept.so2.modules.reports.application

import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import java.util.UUID

object RelatorioSecretariaAcesso {
    const val CAP: String = "report.view_secretary"
    const val MSG_SEM_CAP: String = "Você não tem permissão para esta operação."
    const val MSG_FORA_ESCOPO: String = "Curso fora do escopo da sua secretaria."
    const val MSG_SEM_CURSO: String = "Nenhum curso vinculado à sua secretaria foi encontrado."

    fun exigirCap(authorities: Collection<String>) {
        if (CAP !in authorities) {
            throw AcessoNegadoException(MSG_SEM_CAP)
        }
    }

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
}
