package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.modules.solicitacoes.domain.WorkflowDefinicao
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class ListarFilaDeliberacaoUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val tipoRepository: TipoSolicitacaoRepository,
    private val workflowJsonParser: WorkflowJsonParser,
) {

    @Transactional(readOnly = true)
    fun execute(tipoCodigo: String?, somenteAtraso: Boolean, pageable: Pageable): Page<Solicitacao> {
        val estados = estadosDeliberaveis()
        if (estados.isEmpty()) {
            return Page.empty(limitar(pageable))
        }
        return solicitacaoRepository.findParaDeliberar(
            estados,
            blankToNull(tipoCodigo),
            somenteAtraso,
            OffsetDateTime.now(),
            limitar(pageable),
        )
    }

    private fun estadosDeliberaveis(): Set<String> =
        tipoRepository.findPublishedAll()
            .mapNotNull { tipo ->
                try {
                    workflowJsonParser.parse(tipo.workflowJson)
                } catch (_: Exception) {
                    null
                }
            }
            .flatMap(WorkflowDefinicao::estadosComAcoesDeliberativas)
            .toSet()

    companion object {
        private fun limitar(pageable: Pageable): Pageable {
            val size = minOf(maxOf(pageable.pageSize, 1), 100)
            val sort = if (pageable.sort.isSorted) {
                pageable.sort
            } else {
                Sort.by(Sort.Direction.ASC, "prazoEm")
            }
            return PageRequest.of(pageable.pageNumber, size, sort)
        }

        private fun blankToNull(valor: String?): String? =
            if (valor.isNullOrBlank()) null else valor.trim()
    }
}
