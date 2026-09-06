package br.ufpr.sept.so2.modules.solicitacoes.application;

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository;
import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarRequestTypesUseCase {

    private final TipoSolicitacaoRepository tipoRepository;

    public ListarRequestTypesUseCase(TipoSolicitacaoRepository tipoRepository) {
        this.tipoRepository = tipoRepository;
    }

    @Transactional(readOnly = true)
    public Page<TipoSolicitacao> listarPublicados(Pageable pageable) {
        int size = Math.min(Math.max(pageable.getPageSize(), 1), 100);
        Pageable limitado = PageRequest.of(
                pageable.getPageNumber(),
                size,
                pageable.getSort().isSorted() ? pageable.getSort() : Sort.by("nome")
        );
        return tipoRepository.findPublished(limitado);
    }

    @Transactional(readOnly = true)
    public TipoSolicitacao buscarPublicado(String codigo) {
        TipoSolicitacao tipo = tipoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de solicitação não encontrado."));
        if (!tipo.isPublished()) {
            throw new RecursoNaoEncontradoException("Tipo de solicitação não encontrado.");
        }
        return tipo;
    }
}
