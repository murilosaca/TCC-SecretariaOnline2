package br.ufpr.sept.so2.modules.solicitacoes.application;

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository;
import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo;
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarProtocoloPublicoUseCase {

    private final SolicitacaoRepository solicitacaoRepository;

    public ConsultarProtocoloPublicoUseCase(SolicitacaoRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional(readOnly = true)
    public Solicitacao execute(String protocoloBruto) {
        Protocolo protocolo = Protocolo.of(protocoloBruto);
        return solicitacaoRepository.findByProtocolo(protocolo.getValor())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum protocolo registrado com este identificador."));
    }
}
