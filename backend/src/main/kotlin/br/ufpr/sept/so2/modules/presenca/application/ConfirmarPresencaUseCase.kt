package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaPorPresencaPort
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.modules.presenca.domain.Presenca
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ConfirmarPresencaUseCase(
    private val eventoRepository: EventoRepository,
    private val presencaRepository: PresencaRepository,
    private val passwordHasher: PasswordHasher,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
    private val formativaPorPresencaPort: FormativaPorPresencaPort,
) {
    @Transactional
    fun execute(
        eventoId: UUID,
        usuarioId: UUID,
        pin: String?,
        token: String?,
        deviceUuid: String,
        faseRaw: String,
        ip: String?,
    ): ObterSessaoPresencaUseCase.SessaoPresenca {
        val evento = eventoRepository.findById(eventoId)
            .orElseThrow { RecursoNaoEncontradoException("Evento não encontrado.") }
        val fase = FasePresenca.from(faseRaw)
        if (presencaRepository.existsByEventoUsuarioFase(eventoId, usuarioId, fase)) {
            throw ConflitoEstadoException("A presença desta fase já foi confirmada.")
        }
        if (presencaRepository.existsByEventoAndDeviceDeOutroUsuario(eventoId, deviceUuid, usuarioId)) {
            throw ConflitoEstadoException("Este dispositivo já foi usado neste evento.")
        }
        if (fase == FasePresenca.SAIDA &&
            !presencaRepository.existsByEventoUsuarioFase(eventoId, usuarioId, FasePresenca.ENTRADA)
        ) {
            throw ConflitoEstadoException("Confirmação de saída exige entrada registrada.")
        }
        val informado = if (evento.attendanceMode.isSecret()) pin else token
        val segredoValido = passwordHasher.matches(informado, evento.pinHash)
        if (!segredoValido) {
            passwordHasher.matchesDummy()
        }
        val agora = OffsetDateTime.now()
        evento.garantirConfirmacao(fase, agora, segredoValido)
        val presenca = Presenca.registrar(
            Uuids.v7(),
            eventoId,
            usuarioId,
            fase,
            deviceUuid,
            agora,
        )
        presencaRepository.save(presenca)
        val payload = payload(eventoId, usuarioId, fase, presenca.id)
        outboxPort.enqueue("presenca.confirmada", payload)
        auditLogPort.append("presenca.confirmada", usuarioId, payload, ip)
        if (presencaCompleta(evento, fase)) {
            formativaPorPresencaPort.criarPendenteSeAusente(
                eventoId,
                usuarioId,
                evento.titulo,
                evento.cargaHoraria,
            )
        }
        val fases = presencaRepository.findByEventoAndUsuario(eventoId, usuarioId).map { it.fase }
        return ObterSessaoPresencaUseCase.SessaoPresenca(evento, fases)
    }

    private fun payload(eventoId: UUID, usuarioId: UUID, fase: FasePresenca, presencaId: UUID): String =
        try {
            objectMapper.writeValueAsString(
                mapOf(
                    "eventoId" to eventoId.toString(),
                    "usuarioId" to usuarioId.toString(),
                    "fase" to fase.name,
                    "presencaId" to presencaId.toString(),
                ),
            )
        } catch (_: JsonProcessingException) {
            "{\"eventoId\":\"$eventoId\"}"
        }

    companion object {
        fun presencaCompleta(evento: Evento, fase: FasePresenca): Boolean =
            if (evento.attendanceMode.isDual()) {
                fase == FasePresenca.SAIDA
            } else {
                fase == FasePresenca.ENTRADA
            }
    }
}
