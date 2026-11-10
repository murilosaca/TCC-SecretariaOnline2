package br.ufpr.sept.so2.modules.presenca.infrastructure

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
@Profile("dev")
@Order(20)
class EventoDevDataLoader(
    private val properties: EventoProperties,
    private val eventoRepository: EventoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val passwordHasher: PasswordHasher,
    private val hostPinPort: HostPinPort,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        var pin = properties.devPin
        if (pin.isBlank()) {
            pin = "123456"
        }
        val anfitriaoId = usuarioRepository.findByEmail(EMAIL_PROFESSOR)
            .map { it.id }
            .orElse(null)
        if (anfitriaoId == null) {
            LOG.warn("professor.dev ausente; oficina de presença sem anfitrião.")
        }
        val pinHash = passwordHasher.hash(pin)
        val agora = OffsetDateTime.now()

        val oficina = eventoRepository.findByTitulo(TITULO_SEED)
            .orElseGet { Evento.seedDev(Uuids.v7(), anfitriaoId, TITULO_SEED, pinHash, agora) }
        oficina.renovarJanelaDev(pinHash, anfitriaoId, agora)
        eventoRepository.save(oficina)
        hostPinPort.guardar(oficina.id, pin)

        garantirLab(
            titulo = TITULO_AGENDADO,
            anfitriaoId = anfitriaoId,
            estado = EventoEstado.AGENDADO,
            inicioEm = horarioHoje(agora, 20, 0),
            fimEm = horarioHoje(agora, 22, 0),
            pinHash = null,
            agora = agora,
        )
        garantirLab(
            titulo = TITULO_EM_ANDAMENTO,
            anfitriaoId = anfitriaoId,
            estado = EventoEstado.EM_ANDAMENTO,
            inicioEm = agora.minusMinutes(30),
            fimEm = agora.plusHours(1),
            pinHash = pinHash,
            agora = agora,
        ).also { hostPinPort.guardar(it.id, pin) }
        garantirLab(
            titulo = TITULO_CONCLUIDO,
            anfitriaoId = anfitriaoId,
            estado = EventoEstado.CONCLUIDO,
            inicioEm = horarioHoje(agora, 8, 0),
            fimEm = horarioHoje(agora, 10, 0),
            pinHash = pinHash,
            agora = agora,
        )

        LOG.info(
            "Eventos de presença de desenvolvimento prontos (anfitrião professor.dev: " +
                "oficina + lab AGENDADO/EM_ANDAMENTO/CONCLUIDO).",
        )
    }

    private fun garantirLab(
        titulo: String,
        anfitriaoId: UUID?,
        estado: EventoEstado,
        inicioEm: OffsetDateTime,
        fimEm: OffsetDateTime,
        pinHash: String?,
        agora: OffsetDateTime,
    ): Evento {
        val evento = eventoRepository.findByTitulo(titulo).orElseGet {
            Evento(
                Uuids.v7(),
                anfitriaoId,
                titulo,
                inicioEm,
                fimEm,
                2,
                AttendanceMode.SECRET_SINGLE,
                estado,
                pinHash,
                if (estado == EventoEstado.EM_ANDAMENTO) agora.minusMinutes(5) else null,
                if (estado == EventoEstado.EM_ANDAMENTO) agora.plusMinutes(30) else null,
                null,
                null,
                agora,
                agora,
            )
        }
        evento.idAnfitriao = anfitriaoId
        evento.estado = estado
        evento.inicioEm = inicioEm
        evento.fimEm = fimEm
        evento.pinHash = pinHash
        if (estado == EventoEstado.EM_ANDAMENTO) {
            evento.janelaEntradaInicio = agora.minusMinutes(5)
            evento.janelaEntradaFim = agora.plusMinutes(30)
        } else {
            evento.janelaEntradaInicio = null
            evento.janelaEntradaFim = null
        }
        evento.janelaSaidaInicio = null
        evento.janelaSaidaFim = null
        evento.updatedAt = agora
        return eventoRepository.save(evento)
    }

    companion object {
        const val TITULO_SEED = "Oficina Proof of Stay (dev)"
        const val TITULO_AGENDADO = "[lab] Evento AGENDADO"
        const val TITULO_EM_ANDAMENTO = "[lab] Evento EM_ANDAMENTO"
        const val TITULO_CONCLUIDO = "[lab] Evento CONCLUIDO"
        const val EMAIL_PROFESSOR = "professor.dev@ufpr.br"
        private val LOG = LoggerFactory.getLogger(EventoDevDataLoader::class.java)

        private fun horarioHoje(agora: OffsetDateTime, hora: Int, minuto: Int): OffsetDateTime =
            agora.toLocalDate().atTime(hora, minuto).atOffset(agora.offset)
    }
}
