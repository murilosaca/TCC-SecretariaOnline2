package br.ufpr.sept.so2.modules.solicitacoes.application;

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository;
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListarMinhasSolicitacoesUseCase {

    private final SolicitacaoRepository solicitacaoRepository;

    public ListarMinhasSolicitacoesUseCase(SolicitacaoRepository solicitacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Solicitacao> execute(
            UUID solicitanteId,
            String estado,
            String tipoCodigo,
            Integer ano,
            Pageable pageable
    ) {
        Pageable limitado = limitar(pageable);
        return solicitacaoRepository.findMinhas(
                solicitanteId,
                blankToNull(estado),
                blankToNull(tipoCodigo),
                ano,
                limitado
        );
    }

    private static Pageable limitar(Pageable pageable) {
        int size = Math.min(Math.max(pageable.getPageSize(), 1), 100);
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "createdAt");
        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }

    private static String blankToNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
