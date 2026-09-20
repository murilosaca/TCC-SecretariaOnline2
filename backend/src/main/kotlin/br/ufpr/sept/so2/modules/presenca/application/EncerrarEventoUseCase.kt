package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class EncerrarEventoUseCase(
    private val eventoRepository: EventoRepository,
    private val presencaRepository: PresencaRepository,
    private val hostPinPort: HostPinPort,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(eventoId: UUID, anfitriaoId: UUID, ip: String?): AbrirJanelaUseCase.SessaoHost {
        val evento = eventoRepository.findById(eventoId)
            .orElseThrow { RecursoNaoEncontradoException("Evento não encontrado.") }
        evento.garantirHospedeiro(anfitriaoId)
        evento.encerrar(OffsetDateTime.now())
        eventoRepository.save(evento)
        hostPinPort.limpar(eventoId)
        val payload = payload(eventoId, anfitriaoId)
        outboxPort.enqueue("evento.encerrado", payload)
        auditLogPort.append("evento.encerrado", anfitriaoId, payload, ip)
        return AbrirJanelaUseCase.SessaoHost(evento, null, presencaRepository.countByEvento(eventoId), null)
    }

    private fun payload(eventoId: UUID, anfitriaoId: UUID): String =
        try {
            objectMapper.writeValueAsString(
                mapOf(
                    "eventoId" to eventoId.toString(),
                    "anfitriaoId" to anfitriaoId.toString(),
                ),
            )
        } catch (_: JsonProcessingException) {
            "{\"eventoId\":\"$eventoId\"}"
        }
}
