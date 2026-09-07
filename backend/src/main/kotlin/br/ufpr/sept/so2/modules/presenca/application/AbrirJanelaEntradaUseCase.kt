package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AbrirJanelaEntradaUseCase(
    private val eventoRepository: EventoRepository,
    private val presencaRepository: PresencaRepository,
    private val passwordHasher: PasswordHasher,
    private val hostPinPort: HostPinPort,
    private val outboxPort: OutboxPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(eventoId: UUID, anfitriaoId: UUID): SessaoHost {
        val evento = eventoRepository.findById(eventoId)
            .orElseThrow { RecursoNaoEncontradoException("Evento não encontrado.") }
        evento.garantirHospedeiro(anfitriaoId)
        val pin = PinPresenca.gerar()
        val agora = OffsetDateTime.now()
        evento.abrirJanelaEntrada(passwordHasher.hash(pin), agora, Evento.JANELA_ENTRADA_MINUTOS_PADRAO)
        eventoRepository.save(evento)
        hostPinPort.guardar(eventoId, pin)
        outboxPort.enqueue("evento.janela_aberta", payload(eventoId, anfitriaoId))
        return SessaoHost(evento, pin, presencaRepository.countByEvento(eventoId))
    }

    private fun payload(eventoId: UUID, anfitriaoId: UUID): String =
        try {
            objectMapper.writeValueAsString(
                mapOf(
                    "eventoId" to eventoId.toString(),
                    "anfitriaoId" to anfitriaoId.toString(),
                    "fase" to "ENTRADA",
                ),
            )
        } catch (_: JsonProcessingException) {
            "{\"eventoId\":\"$eventoId\"}"
        }

    data class SessaoHost(val evento: Evento, val pin: String?, val presentes: Long)
}
