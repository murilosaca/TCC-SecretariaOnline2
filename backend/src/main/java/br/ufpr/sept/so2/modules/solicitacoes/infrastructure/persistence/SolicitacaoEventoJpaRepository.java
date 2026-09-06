package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SolicitacaoEventoJpaRepository extends JpaRepository<SolicitacaoEventoJpaEntity, UUID> {

    List<SolicitacaoEventoJpaEntity> findBySolicitacaoIdOrderByCreatedAtDesc(UUID solicitacaoId);
}
