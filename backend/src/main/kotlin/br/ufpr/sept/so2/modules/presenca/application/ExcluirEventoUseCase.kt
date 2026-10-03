package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Exclusão só do evento AGENDADO e sem presença: 409 em EM_ANDAMENTO ou
 * CONCLUIDO, 422 quando já existe registro de presença.
 */
@Service
class ExcluirEventoUseCase(
    private val eventoRepository: EventoRepository,
    private val presencaRepository: PresencaRepository,
    private val cursoEscopoPort: CursoEscopoPort,
) {
    @Transactional
    fun execute(id: UUID, atorId: UUID, authorities: List<String>) {
        val evento = EventoEdicao.carregar(id, eventoRepository, atorId, authorities, cursoEscopoPort)
        evento.garantirExclusao(presencaRepository.countByEvento(evento.id) > 0)
        eventoRepository.deleteById(evento.id)
    }
}
