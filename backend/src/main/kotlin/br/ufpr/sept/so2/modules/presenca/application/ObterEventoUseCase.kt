package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObterEventoUseCase(
    private val eventoRepository: EventoRepository,
) {
    @Transactional(readOnly = true)
    fun execute(id: UUID): Evento = eventoRepository.findById(id)
        .orElseThrow { RecursoNaoEncontradoException("Evento não encontrado.") }
}
