package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObterSessaoPresencaUseCase(
    private val eventoRepository: EventoRepository,
    private val presencaRepository: PresencaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(eventoId: UUID, usuarioId: UUID): SessaoPresenca {
        val evento = eventoRepository.findById(eventoId)
            .orElseThrow { RecursoNaoEncontradoException("Evento não encontrado.") }
        val fases = presencaRepository.findByEventoAndUsuario(eventoId, usuarioId).map { it.fase }
        return SessaoPresenca(evento, fases)
    }

    data class SessaoPresenca(val evento: Evento, val fasesConfirmadas: List<FasePresenca>)
}
