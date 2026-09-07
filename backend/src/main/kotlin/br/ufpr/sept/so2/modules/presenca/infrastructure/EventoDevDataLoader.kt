package br.ufpr.sept.so2.modules.presenca.infrastructure

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

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
        val evento = eventoRepository.findByTitulo(TITULO_SEED)
            .orElseGet { Evento.seedDev(Uuids.v7(), anfitriaoId, TITULO_SEED, pinHash, agora) }
        evento.renovarJanelaDev(pinHash, anfitriaoId, agora)
        eventoRepository.save(evento)
        hostPinPort.guardar(evento.id, pin)
        LOG.info("Evento de presença de desenvolvimento pronto (anfitrião professor.dev, janela 30 min).")
    }

    companion object {
        const val TITULO_SEED = "Oficina Proof of Stay (dev)"
        const val EMAIL_PROFESSOR = "professor.dev@ufpr.br"
        private val LOG = LoggerFactory.getLogger(EventoDevDataLoader::class.java)
    }
}
