package br.ufpr.sept.so2.modules.reports.application

import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import java.util.UUID

object RelatorioCoordenadorAcesso {
    const val CAP: String = "report.view_coordinator"
    const val MSG_SEM_CAP: String = "Você não tem permissão para esta operação."
    const val MSG_OUTRO_CURSO: String = "Você não é coordenador deste curso."
    const val MSG_SEM_CURSO: String = "Nenhum curso sob sua coordenação foi encontrado."

    fun exigirCap(authorities: Collection<String>) {
        if (CAP !in authorities) {
            throw AcessoNegadoException(MSG_SEM_CAP)
        }
    }

    fun exigirDono(cursoId: UUID?, coordenados: Set<UUID>): UUID {
        if (coordenados.isEmpty()) {
            throw AcessoNegadoException(MSG_SEM_CURSO)
        }
        if (cursoId == null) {
            if (coordenados.size == 1) {
                return coordenados.first()
            }
            throw AcessoNegadoException(MSG_OUTRO_CURSO)
        }
        if (cursoId !in coordenados) {
            throw AcessoNegadoException(MSG_OUTRO_CURSO)
        }
        return cursoId
    }
}
