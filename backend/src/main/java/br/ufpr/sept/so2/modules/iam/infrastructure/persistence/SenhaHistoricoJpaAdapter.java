package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import br.ufpr.sept.so2.modules.iam.application.ports.SenhaHistoricoRepository;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class SenhaHistoricoJpaAdapter implements SenhaHistoricoRepository {

    private final SenhaHistoricoJpaRepository jpaRepository;

    public SenhaHistoricoJpaAdapter(SenhaHistoricoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void append(UUID usuarioId, String senhaHash) {
        jpaRepository.save(new SenhaHistoricoJpaEntity(Uuids.v7(), usuarioId, senhaHash, OffsetDateTime.now()));
    }

    @Override
    public List<String> findLastHashes(UUID usuarioId, int limite) {
        return jpaRepository.findTop3ByUsuarioIdOrderByCreatedAtDesc(usuarioId).stream()
                .limit(limite)
                .map(SenhaHistoricoJpaEntity::getSenhaHash)
                .toList();
    }
}
