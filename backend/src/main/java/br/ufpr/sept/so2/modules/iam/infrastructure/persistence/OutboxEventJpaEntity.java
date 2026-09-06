package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_event")
public class OutboxEventJpaEntity extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private int tentativas;

    protected OutboxEventJpaEntity() {
    }

    public OutboxEventJpaEntity(String tipo, String payload) {
        this.tipo = tipo;
        this.payload = payload;
        this.status = "PENDING";
        this.tentativas = 0;
    }
}
