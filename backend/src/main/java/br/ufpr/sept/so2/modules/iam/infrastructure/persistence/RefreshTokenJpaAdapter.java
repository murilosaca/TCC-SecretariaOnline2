package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository;
import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenJpaAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenJpaAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshSessao save(RefreshSessao sessao) {
        RefreshTokenJpaEntity entity = jpaRepository.findById(sessao.getId())
                .orElseGet(() -> RefreshTokenJpaEntity.fromDomain(sessao));
        entity.merge(sessao);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<RefreshSessao> lockByTokenHash(String tokenHash) {
        return jpaRepository.lockByTokenHash(tokenHash).map(RefreshTokenJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUsuarioId(UUID usuarioId) {
        jpaRepository.revokeAllByUsuarioId(usuarioId);
    }
}
