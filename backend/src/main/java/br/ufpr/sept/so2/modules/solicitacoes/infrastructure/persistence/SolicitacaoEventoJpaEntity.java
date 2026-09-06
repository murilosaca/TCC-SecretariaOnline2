package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence;

import br.ufpr.sept.so2.modules.solicitacoes.domain.SolicitacaoEvento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "solicitacao_evento")
public class SolicitacaoEventoJpaEntity {

    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "solicitacao_id", nullable = false)
    private UUID solicitacaoId;

    @Column(nullable = false, length = 80)
    private String tipo;

    @Column(name = "estado_de", length = 40)
    private String estadoDe;

    @Column(name = "estado_para", nullable = false, length = 40)
    private String estadoPara;

    @Column(name = "ator_id")
    private UUID atorId;

    @Column(columnDefinition = "text")
    private String parecer;

    @Column(columnDefinition = "text")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected SolicitacaoEventoJpaEntity() {
    }

    public static SolicitacaoEventoJpaEntity fromDomain(UUID solicitacaoId, SolicitacaoEvento evento) {
        SolicitacaoEventoJpaEntity entity = new SolicitacaoEventoJpaEntity();
        entity.id = evento.id();
        entity.solicitacaoId = solicitacaoId;
        entity.tipo = evento.tipo();
        entity.estadoDe = evento.estadoDe();
        entity.estadoPara = evento.estadoPara();
        entity.atorId = evento.atorId();
        entity.parecer = evento.parecer();
        entity.payload = evento.payload();
        entity.createdAt = evento.createdAt();
        return entity;
    }

    public SolicitacaoEvento toDomain() {
        return new SolicitacaoEvento(id, tipo, estadoDe, estadoPara, atorId, parecer, payload, createdAt);
    }

    public UUID getId() {
        return id;
    }
}
