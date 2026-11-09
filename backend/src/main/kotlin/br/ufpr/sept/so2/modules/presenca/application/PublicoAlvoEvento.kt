package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Público-alvo do evento é o curso informado na criação. Evento sem curso
 * (legado) continua visível. Curso diferente responde como se o evento não existisse.
 */
@Service
class PublicoAlvoEvento(
    private val alunoRepository: AlunoRepository,
) {
    fun cursoDe(usuarioId: UUID): UUID? =
        alunoRepository.findByIdUsuario(usuarioId).map { it.idCurso }.orElse(null)

    fun exigir(evento: Evento, usuarioId: UUID) {
        val curso = evento.idCurso ?: return
        val doAluno = cursoDe(usuarioId)
        if (doAluno == null || doAluno != curso) {
            throw RecursoNaoEncontradoException("Evento não encontrado.")
        }
    }
}
