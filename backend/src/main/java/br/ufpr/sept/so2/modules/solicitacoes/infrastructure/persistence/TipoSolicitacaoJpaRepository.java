package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence;

import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TipoSolicitacaoJpaRepository extends JpaRepository<TipoSolicitacaoJpaEntity, UUID> {

    Optional<TipoSolicitacaoJpaEntity> findByCodigoIgnoreCase(String codigo);

    Page<TipoSolicitacaoJpaEntity> findByStatus(String status, Pageable pageable);

    default Page<TipoSolicitacaoJpaEntity> findPublished(Pageable pageable) {
        return findByStatus(TipoSolicitacao.PUBLISHED, pageable);
    }
}
