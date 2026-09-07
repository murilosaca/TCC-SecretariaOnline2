package br.ufpr.sept.so2.modules.presenca.domain;

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Presenca {

    private final UUID id;
    private final UUID eventoId;
    private final UUID usuarioId;
    private final FasePresenca fase;
    private final String deviceUuid;
    private final OffsetDateTime instante;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public Presenca(
            UUID id,
            UUID eventoId,
            UUID usuarioId,
            FasePresenca fase,
            String deviceUuid,
            OffsetDateTime instante,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        if (deviceUuid == null || deviceUuid.isBlank()) {
            throw new DadoInvalidoException("Identificador do dispositivo é obrigatório.");
        }
        this.id = id;
        this.eventoId = eventoId;
        this.usuarioId = usuarioId;
        this.fase = fase;
        this.deviceUuid = deviceUuid.trim();
        this.instante = instante;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Presenca registrar(
            UUID id,
            UUID eventoId,
            UUID usuarioId,
            FasePresenca fase,
            String deviceUuid,
            OffsetDateTime agora
    ) {
        return new Presenca(id, eventoId, usuarioId, fase, deviceUuid, agora, agora, agora);
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public FasePresenca getFase() {
        return fase;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public OffsetDateTime getInstante() {
        return instante;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
