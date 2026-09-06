package br.ufpr.sept.so2.modules.solicitacoes.application.ports;

import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoRepository {

    Solicitacao save(Solicitacao solicitacao);

    Optional<Solicitacao> findById(UUID id);

    Optional<Solicitacao> findByProtocolo(String protocolo);

    Page<Solicitacao> findMinhas(
            UUID solicitanteId,
            String estado,
            String tipoCodigo,
            Integer ano,
            Pageable pageable
    );
}
