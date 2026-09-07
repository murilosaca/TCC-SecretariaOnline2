package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ObterSessaoHostUseCase(
    private val eventoRepository: EventoRepository,
    private val presencaRepository: PresencaRepository,
    private val hostPinPort: HostPinPort,
) {
    @Transactional(readOnly = true)
    fun execute(eventoId: UUID, anfitriaoId: UUID): AbrirJanelaEntradaUseCase.SessaoHost {
        val evento = eventoRepository.findById(eventoId)
            .orElseThrow { RecursoNaoEncontradoException("Evento não encontrado.") }
        evento.garantirHospedeiro(anfitriaoId)
        val pin = if (evento.janelaAtiva(FasePresenca.ENTRADA, OffsetDateTime.now())) {
            hostPinPort.obter(eventoId).orElse(null)
        } else {
            null
        }
        return AbrirJanelaEntradaUseCase.SessaoHost(evento, pin, presencaRepository.countByEvento(eventoId))
    }
}
