package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.estagio.application.ports.CoeMembroPort
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import java.util.UUID

internal object CoeAcesso {
    const val REVIEW = "internship.review"
    const val ATRIBUIR = "assign-member"

    fun cursosDoMembro(port: CoeMembroPort, usuarioId: UUID): Set<UUID> =
        port.cursosDoMembro(usuarioId)

    fun exigirNoEscopo(estagio: Estagio, cursos: Set<UUID>) {
        if (estagio.idCurso !in cursos) {
            throw RecursoNaoEncontradoException("Estágio não encontrado.")
        }
    }
}
