package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JtiBlacklistJpaRepository extends JpaRepository<JtiBlacklistJpaEntity, String> {
}
