package br.ufpr.sept.so2.modules.coordenacao.application

import br.ufpr.sept.so2.modules.coordenacao.application.ports.CursoConfigPort.CursoResumo
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import java.util.UUID

object CursoConfigAcesso {
    const val CAP: String = "course.config"
    const val MSG_SEM_CAP: String = "Você não tem permissão para esta operação."
    const val MSG_OUTRO_CURSO: String = "Você não é coordenador deste curso."

    fun exigirCap(authorities: Collection<String>) {
        if (CAP !in authorities) {
            throw AcessoNegadoException(MSG_SEM_CAP)
        }
    }

    fun exigirDono(curso: CursoResumo?, usuarioId: UUID): CursoResumo {
        if (curso == null || curso.idCoordenador != usuarioId) {
            throw AcessoNegadoException(MSG_OUTRO_CURSO)
        }
        return curso
    }
}
