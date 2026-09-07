package br.ufpr.sept.so2.modules.presenca.application;

import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort;
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository;
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca;
import br.ufpr.sept.so2.modules.presenca.domain.Presenca;
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ConfirmarPresencaUseCase {

    private final EventoRepository eventoRepository;
    private final PresencaRepository presencaRepository;
    private final PasswordHasher passwordHasher;
    private final OutboxPort outboxPort;
    private final ObjectMapper objectMapper;

    public ConfirmarPresencaUseCase(
            EventoRepository eventoRepository,
            PresencaRepository presencaRepository,
            PasswordHasher passwordHasher,
            OutboxPort outboxPort,
            ObjectMapper objectMapper
    ) {
        this.eventoRepository = eventoRepository;
        this.presencaRepository = presencaRepository;
        this.passwordHasher = passwordHasher;
        this.outboxPort = outboxPort;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ObterSessaoPresencaUseCase.SessaoPresenca execute(
            UUID eventoId,
            UUID usuarioId,
            String pin,
            String deviceUuid,
            String faseRaw
    ) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));
        FasePresenca fase = FasePresenca.from(faseRaw);
        if (presencaRepository.existsByEventoUsuarioFase(eventoId, usuarioId, fase)) {
            throw new ConflitoEstadoException("A presença desta fase já foi confirmada.");
        }
        if (presencaRepository.existsByEventoAndDeviceDeOutroUsuario(eventoId, deviceUuid, usuarioId)) {
            throw new ConflitoEstadoException("Este dispositivo já foi usado neste evento.");
        }
        boolean pinValido = passwordHasher.matches(pin, evento.getPinHash());
        if (!pinValido) {
            passwordHasher.matchesDummy();
        }
        evento.garantirConfirmacao(AttendanceMode.SECRET_SINGLE, fase, OffsetDateTime.now(), pinValido);
        Presenca presenca = Presenca.registrar(
                Uuids.v7(),
                eventoId,
                usuarioId,
                fase,
                deviceUuid,
                OffsetDateTime.now()
        );
        presencaRepository.save(presenca);
        outboxPort.enqueue("presenca.confirmada", payload(eventoId, usuarioId, fase, presenca.getId()));
        List<FasePresenca> fases = presencaRepository.findByEventoAndUsuario(eventoId, usuarioId).stream()
                .map(Presenca::getFase)
                .toList();
        return new ObterSessaoPresencaUseCase.SessaoPresenca(evento, fases);
    }

    private String payload(UUID eventoId, UUID usuarioId, FasePresenca fase, UUID presencaId) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "eventoId", eventoId.toString(),
                    "usuarioId", usuarioId.toString(),
                    "fase", fase.name(),
                    "presencaId", presencaId.toString()
            ));
        } catch (JsonProcessingException ex) {
            return "{\"eventoId\":\"" + eventoId + "\"}";
        }
    }
}
