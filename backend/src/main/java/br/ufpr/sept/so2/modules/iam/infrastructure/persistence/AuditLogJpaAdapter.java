package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class AuditLogJpaAdapter implements AuditLogPort {

    private final AuditLogJpaRepository jpaRepository;

    public AuditLogJpaAdapter(AuditLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void append(String tipo, UUID atorId, String payload, String ip) {
        jpaRepository.save(new AuditLogJpaEntity(
                Uuids.v7(),
                tipo,
                atorId,
                payload == null ? "" : payload,
                ip,
                OffsetDateTime.now()
        ));
    }
}
