package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort;
import org.springframework.stereotype.Component;

@Component
public class OutboxJpaAdapter implements OutboxPort {

    private final OutboxEventJpaRepository jpaRepository;

    public OutboxJpaAdapter(OutboxEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void enqueue(String tipo, String payload) {
        jpaRepository.save(new OutboxEventJpaEntity(tipo, payload));
    }
}
