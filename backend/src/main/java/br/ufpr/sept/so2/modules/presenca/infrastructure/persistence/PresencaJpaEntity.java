package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence;

import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca;
import br.ufpr.sept.so2.modules.presenca.domain.Presenca;
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "presenca",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_presenca_evento_usuario_fase",
                columnNames = {"evento_id", "usuario_id", "fase"}
        )
)
public class PresencaJpaEntity extends BaseEntity {

    @Column(name = "evento_id", nullable = false)
    private UUID eventoId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false, length = 20)
    private String fase;

    @Column(name = "device_uuid", nullable = false, length = 64)
    private String deviceUuid;

    @Column(nullable = false)
    private OffsetDateTime instante;

    protected PresencaJpaEntity() {
    }

    public static PresencaJpaEntity fromDomain(Presenca presenca) {
        PresencaJpaEntity entity = new PresencaJpaEntity();
        entity.setId(presenca.getId());
        entity.eventoId = presenca.getEventoId();
        entity.usuarioId = presenca.getUsuarioId();
        entity.fase = presenca.getFase().name();
        entity.deviceUuid = presenca.getDeviceUuid();
        entity.instante = presenca.getInstante();
        return entity;
    }

    public Presenca toDomain() {
        return new Presenca(
                getId(),
                eventoId,
                usuarioId,
                FasePresenca.from(fase),
                deviceUuid,
                instante,
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
