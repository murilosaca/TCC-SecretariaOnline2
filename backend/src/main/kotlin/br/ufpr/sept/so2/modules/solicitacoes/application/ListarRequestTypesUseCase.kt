package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListarRequestTypesUseCase(
    private val tipoRepository: TipoSolicitacaoRepository,
) {

    @Transactional(readOnly = true)
    fun listarPublicados(pageable: Pageable): Page<TipoSolicitacao> {
        val size = minOf(maxOf(pageable.pageSize, 1), 100)
        val limitado = PageRequest.of(
            pageable.pageNumber,
            size,
            if (pageable.sort.isSorted) pageable.sort else Sort.by("nome"),
        )
        return tipoRepository.findPublished(limitado)
    }

    @Transactional(readOnly = true)
    fun buscarPublicado(codigo: String): TipoSolicitacao {
        val tipo = tipoRepository.findByCodigo(codigo)
            .orElseThrow { RecursoNaoEncontradoException("Tipo de solicitação não encontrado.") }
        if (!tipo.isPublished()) {
            throw RecursoNaoEncontradoException("Tipo de solicitação não encontrado.")
        }
        return tipo
    }
}
