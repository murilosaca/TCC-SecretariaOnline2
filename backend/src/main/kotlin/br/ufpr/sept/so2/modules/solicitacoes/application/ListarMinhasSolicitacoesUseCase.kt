package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarMinhasSolicitacoesUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
) {

    @Transactional(readOnly = true)
    fun execute(
        solicitanteId: UUID,
        estado: String?,
        tipoCodigo: String?,
        ano: Int?,
        pageable: Pageable,
    ): Page<Solicitacao> {
        val limitado = limitar(pageable)
        return solicitacaoRepository.findMinhas(
            solicitanteId,
            blankToNull(estado),
            blankToNull(tipoCodigo),
            ano,
            limitado,
        )
    }

    companion object {
        private fun limitar(pageable: Pageable): Pageable {
            val size = minOf(maxOf(pageable.pageSize, 1), 100)
            val sort = if (pageable.sort.isSorted) {
                pageable.sort
            } else {
                Sort.by(Sort.Direction.DESC, "createdAt")
            }
            return PageRequest.of(pageable.pageNumber, size, sort)
        }

        private fun blankToNull(valor: String?): String? =
            if (valor.isNullOrBlank()) null else valor.trim()
    }
}
