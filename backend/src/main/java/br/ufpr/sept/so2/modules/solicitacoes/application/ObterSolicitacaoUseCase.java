package br.ufpr.sept.so2.modules.solicitacoes.application;

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository;
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ObterSolicitacaoUseCase {

    private final SolicitacaoRepository solicitacaoRepository;

    public ObterSolicitacaoUseCase(SolicitacaoRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional(readOnly = true)
    public Solicitacao execute(UUID id, UUID solicitanteId) {
        Solicitacao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação não encontrada."));
        if (!solicitacao.pertenceA(solicitanteId)) {
            throw new RecursoNaoEncontradoException("Solicitação não encontrada.");
        }
        return solicitacao;
    }
}
