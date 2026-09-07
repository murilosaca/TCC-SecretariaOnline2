package br.ufpr.sept.so2.modules.presenca.application;

import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort;
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort;
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AbrirJanelaEntradaUseCase {

    private final EventoRepository eventoRepository;
    private final PresencaRepository presencaRepository;
    private final PasswordHasher passwordHasher;
    private final HostPinPort hostPinPort;
    private final OutboxPort outboxPort;
    private final ObjectMapper objectMapper;

    public AbrirJanelaEntradaUseCase(
            EventoRepository eventoRepository,
            PresencaRepository presencaRepository,
            PasswordHasher passwordHasher,
            HostPinPort hostPinPort,
            OutboxPort outboxPort,
            ObjectMapper objectMapper
    ) {
        this.eventoRepository = eventoRepository;
        this.presencaRepository = presencaRepository;
        this.passwordHasher = passwordHasher;
        this.hostPinPort = hostPinPort;
        this.outboxPort = outboxPort;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SessaoHost execute(UUID eventoId, UUID anfitriaoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));
        evento.garantirHospedeiro(anfitriaoId);
        String pin = PinPresenca.gerar();
        OffsetDateTime agora = OffsetDateTime.now();
        evento.abrirJanelaEntrada(passwordHasher.hash(pin), agora, Evento.JANELA_ENTRADA_MINUTOS_PADRAO);
        eventoRepository.save(evento);
        hostPinPort.guardar(eventoId, pin);
        outboxPort.enqueue("evento.janela_aberta", payload(eventoId, anfitriaoId));
        return new SessaoHost(evento, pin, presencaRepository.countByEvento(eventoId));
    }

    private String payload(UUID eventoId, UUID anfitriaoId) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "eventoId", eventoId.toString(),
                    "anfitriaoId", anfitriaoId.toString(),
                    "fase", "ENTRADA"
            ));
        } catch (JsonProcessingException ex) {
            return "{\"eventoId\":\"" + eventoId + "\"}";
        }
    }

    public record SessaoHost(Evento evento, String pin, long presentes) {
    }
}
