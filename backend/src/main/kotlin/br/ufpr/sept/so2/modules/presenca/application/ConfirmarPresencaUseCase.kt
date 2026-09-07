package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
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
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        eventoId: UUID,
        usuarioId: UUID,
        pin: String,
        deviceUuid: String,
        faseRaw: String,
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
        val pinValido = passwordHasher.matches(pin, evento.pinHash)
        if (!pinValido) {
            passwordHasher.matchesDummy()
        }
        evento.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, fase, OffsetDateTime.now(), pinValido)
        val presenca = Presenca.registrar(
            Uuids.v7(),
            eventoId,
            usuarioId,
            fase,
            deviceUuid,
            OffsetDateTime.now(),
        )
        presencaRepository.save(presenca)
        outboxPort.enqueue("presenca.confirmada", payload(eventoId, usuarioId, fase, presenca.id))
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
}
