package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.util.UUID

/**
 * Recorte por curso dos eventos da secretaria (F5.14). Quem opera sem
 * `event.view_curso` — o professor — continua sem recorte de curso.
 */
internal object EventoAcesso {
    const val VIEW_CURSO = "event.view_curso"
    const val MSG_FORA_ESCOPO = "Curso fora do escopo da sua secretaria."
    const val MSG_SEM_CURSO = "Nenhum curso vinculado à sua secretaria foi encontrado."
    const val MSG_CURSO_OBRIGATORIO = "Curso do evento é obrigatório."

    fun cursos(port: CursoEscopoPort, atorId: UUID): Set<UUID> {
        val vinculados = port.cursoIdsDoUsuario(atorId)
        if (vinculados.isEmpty()) {
            throw AcessoNegadoException(MSG_SEM_CURSO)
        }
        return vinculados
    }

    fun exigirCursoNoEscopo(cursoId: UUID?, escopo: Set<UUID>): UUID {
        if (cursoId == null) {
            throw DadoInvalidoException(MSG_CURSO_OBRIGATORIO)
        }
        if (cursoId !in escopo) {
            throw AcessoNegadoException(MSG_FORA_ESCOPO)
        }
        return cursoId
    }

    /** Anfitrião ou evento do curso da secretaria. Sem curso vinculado, só o anfitrião lê. */
    fun alcanca(evento: Evento, atorId: UUID, authorities: List<String>, port: CursoEscopoPort): Boolean {
        if (evento.eAnfitriao(atorId)) {
            return true
        }
        val curso = evento.idCurso
        return VIEW_CURSO in authorities && curso != null && curso in port.cursoIdsDoUsuario(atorId)
    }
}
