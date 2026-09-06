package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence;

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository;
import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TipoSolicitacaoJpaAdapter implements TipoSolicitacaoRepository {

    private final TipoSolicitacaoJpaRepository jpaRepository;

    public TipoSolicitacaoJpaAdapter(TipoSolicitacaoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TipoSolicitacao save(TipoSolicitacao tipo) {
        TipoSolicitacaoJpaEntity entity = jpaRepository.findById(tipo.getId())
                .orElseGet(() -> TipoSolicitacaoJpaEntity.fromDomain(tipo));
        entity.merge(tipo);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public Optional<TipoSolicitacao> findById(UUID id) {
        return jpaRepository.findById(id).map(TipoSolicitacaoJpaEntity::toDomain);
    }

    @Override
    public Optional<TipoSolicitacao> findByCodigo(String codigo) {
        return jpaRepository.findByCodigoIgnoreCase(codigo).map(TipoSolicitacaoJpaEntity::toDomain);
    }

    @Override
    public Page<TipoSolicitacao> findPublished(Pageable pageable) {
        return jpaRepository.findPublished(pageable).map(TipoSolicitacaoJpaEntity::toDomain);
    }
}
