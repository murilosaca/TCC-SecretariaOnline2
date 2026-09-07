package br.ufpr.sept.so2.modules.presenca.infrastructure;

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@Profile("dev")
@Order(20)
public class EventoDevDataLoader implements ApplicationRunner {

    public static final String TITULO_SEED = "Oficina Proof of Stay (dev)";
    public static final String EMAIL_PROFESSOR = "professor.dev@ufpr.br";

    private static final Logger LOG = LoggerFactory.getLogger(EventoDevDataLoader.class);

    private final EventoProperties properties;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final HostPinPort hostPinPort;

    public EventoDevDataLoader(
            EventoProperties properties,
            EventoRepository eventoRepository,
            UsuarioRepository usuarioRepository,
            PasswordHasher passwordHasher,
            HostPinPort hostPinPort
    ) {
        this.properties = properties;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.hostPinPort = hostPinPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        String pin = properties.getDevPin();
        if (pin == null || pin.isBlank()) {
            pin = "123456";
        }
        UUID anfitriaoId = usuarioRepository.findByEmail(EMAIL_PROFESSOR)
                .map(Usuario::getId)
                .orElse(null);
        if (anfitriaoId == null) {
            LOG.warn("professor.dev ausente; oficina de presença sem anfitrião.");
        }
        String pinHash = passwordHasher.hash(pin);
        OffsetDateTime agora = OffsetDateTime.now();
        Evento evento = eventoRepository.findByTitulo(TITULO_SEED)
                .orElseGet(() -> Evento.seedDev(Uuids.v7(), anfitriaoId, TITULO_SEED, pinHash, agora));
        evento.renovarJanelaDev(pinHash, anfitriaoId, agora);
        eventoRepository.save(evento);
        hostPinPort.guardar(evento.getId(), pin);
        LOG.info("Evento de presença de desenvolvimento pronto (anfitrião professor.dev, janela 30 min).");
    }
}
