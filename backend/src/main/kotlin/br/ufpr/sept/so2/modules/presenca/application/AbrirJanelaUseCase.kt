package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AbrirJanelaUseCase(
    private val eventoRepository: EventoRepository,
    private val presencaRepository: PresencaRepository,
    private val passwordHasher: PasswordHasher,
    private val hostPinPort: HostPinPort,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(eventoId: UUID, anfitriaoId: UUID, fase: FasePresenca, ip: String?): SessaoHost {
        val evento = carregar(eventoId, anfitriaoId)
        val agora = OffsetDateTime.now()
        val segredo = gerarSegredo(evento)
        evento.abrirJanela(fase, passwordHasher.hash(segredo), agora, Evento.JANELA_MINUTOS_PADRAO)
        persistirSegredo(evento, anfitriaoId, segredo, "evento.janela_aberta", fase.name, ip)
        return sessao(evento, segredo)
    }

    @Transactional
    fun renovarQr(eventoId: UUID, anfitriaoId: UUID, ip: String?): SessaoHost {
        val evento = carregar(eventoId, anfitriaoId)
        val agora = OffsetDateTime.now()
        val segredo = SegredoPresenca.qrToken()
        evento.renovarSegredo(passwordHasher.hash(segredo), agora)
        persistirSegredo(evento, anfitriaoId, segredo, "evento.qr_renovado", evento.faseDaJanelaAtiva(agora)?.name, ip)
        return sessao(evento, segredo)
    }

    private fun carregar(eventoId: UUID, anfitriaoId: UUID): Evento {
        val evento = eventoRepository.findById(eventoId)
            .orElseThrow { RecursoNaoEncontradoException("Evento não encontrado.") }
        evento.garantirHospedeiro(anfitriaoId)
        return evento
    }

    private fun gerarSegredo(evento: Evento): String =
        if (evento.attendanceMode.isSecret()) SegredoPresenca.pin() else SegredoPresenca.qrToken()

    private fun persistirSegredo(
        evento: Evento,
        anfitriaoId: UUID,
        segredo: String,
        tipo: String,
        fase: String?,
        ip: String?,
    ) {
        eventoRepository.save(evento)
        hostPinPort.guardar(evento.id, segredo)
        val payload = payload(evento.id, anfitriaoId, fase)
        outboxPort.enqueue(tipo, payload)
        auditLogPort.append(tipo, anfitriaoId, payload, ip)
    }

    private fun sessao(evento: Evento, segredo: String): SessaoHost =
        SessaoHost(
            evento,
            segredo,
            presencaRepository.countByEvento(evento.id),
            hostPinPort.emitidoEm(evento.id).orElse(null),
        )

    private fun payload(eventoId: UUID, anfitriaoId: UUID, fase: String?): String =
        try {
            objectMapper.writeValueAsString(
                mapOf(
                    "eventoId" to eventoId.toString(),
                    "anfitriaoId" to anfitriaoId.toString(),
                    "fase" to fase,
                ),
            )
        } catch (_: JsonProcessingException) {
            "{\"eventoId\":\"$eventoId\"}"
        }

    data class SessaoHost(
        val evento: Evento,
        val segredo: String?,
        val presentes: Long,
        val segredoEmitidoEm: Instant? = null,
    )
}
