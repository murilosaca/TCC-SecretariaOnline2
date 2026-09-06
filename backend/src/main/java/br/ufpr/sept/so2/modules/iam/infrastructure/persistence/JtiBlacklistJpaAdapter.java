package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import br.ufpr.sept.so2.modules.iam.application.ports.JtiBlacklistRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class JtiBlacklistJpaAdapter implements JtiBlacklistRepository {

    private final JtiBlacklistJpaRepository jpaRepository;

    public JtiBlacklistJpaAdapter(JtiBlacklistJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void add(String jti, OffsetDateTime expiresAt) {
        jpaRepository.save(new JtiBlacklistJpaEntity(jti, expiresAt, OffsetDateTime.now()));
    }

    @Override
    public boolean contains(String jti) {
        return jpaRepository.existsById(jti);
    }
}
