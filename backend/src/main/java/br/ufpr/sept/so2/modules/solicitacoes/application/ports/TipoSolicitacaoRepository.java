package br.ufpr.sept.so2.modules.solicitacoes.application.ports;

import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TipoSolicitacaoRepository {

    TipoSolicitacao save(TipoSolicitacao tipo);

    Optional<TipoSolicitacao> findById(UUID id);

    Optional<TipoSolicitacao> findByCodigo(String codigo);

    Page<TipoSolicitacao> findPublished(Pageable pageable);
}
