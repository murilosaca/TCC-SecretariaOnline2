package br.ufpr.sept.so2.modules.presenca.application;

import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort;
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
public class EncerrarEventoUseCase {

    private final EventoRepository eventoRepository;
    private final PresencaRepository presencaRepository;
    private final HostPinPort hostPinPort;
    private final OutboxPort outboxPort;
    private final ObjectMapper objectMapper;

    public EncerrarEventoUseCase(
            EventoRepository eventoRepository,
            PresencaRepository presencaRepository,
            HostPinPort hostPinPort,
            OutboxPort outboxPort,
            ObjectMapper objectMapper
    ) {
        this.eventoRepository = eventoRepository;
        this.presencaRepository = presencaRepository;
        this.hostPinPort = hostPinPort;
        this.outboxPort = outboxPort;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AbrirJanelaEntradaUseCase.SessaoHost execute(UUID eventoId, UUID anfitriaoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));
        evento.garantirHospedeiro(anfitriaoId);
        evento.encerrar(OffsetDateTime.now());
        eventoRepository.save(evento);
        hostPinPort.limpar(eventoId);
        outboxPort.enqueue("evento.encerrado", payload(eventoId, anfitriaoId));
        return new AbrirJanelaEntradaUseCase.SessaoHost(evento, null, presencaRepository.countByEvento(eventoId));
    }

    private String payload(UUID eventoId, UUID anfitriaoId) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "eventoId", eventoId.toString(),
                    "anfitriaoId", anfitriaoId.toString()
            ));
        } catch (JsonProcessingException ex) {
            return "{\"eventoId\":\"" + eventoId + "\"}";
        }
    }
}
