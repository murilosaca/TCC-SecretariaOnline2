package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Fila central da secretaria (F5.2 / F5.5): escopo pelos cursos vinculados.
 */
@Service
class ListarFilaCursoUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val solicitacaoCursoEscopo: SolicitacaoCursoEscopo,
) {

    @Transactional(readOnly = true)
    fun execute(
        atorId: UUID,
        estado: String?,
        tipoCodigo: String?,
        cursoId: UUID?,
        slaBreached: Boolean,
        pageable: Pageable,
    ): Page<Solicitacao> {
        val solicitanteIds = solicitacaoCursoEscopo.solicitanteIdsNoEscopo(atorId, cursoId)
        val estados = resolverEstados(estado)
        return solicitacaoRepository.findFilaCurso(
            solicitanteIds,
            estados,
            blankToNull(tipoCodigo),
            slaBreached,
            OffsetDateTime.now(),
            limitar(pageable),
        )
    }

    companion object {
        /** Alias de negócio: solicitações ainda em trâmite. */
        val ESTADOS_ABERTA: Set<String> = setOf("EM_ANALISE", "EM_AJUSTE")

        private fun resolverEstados(estado: String?): Collection<String>? {
            val limpo = blankToNull(estado) ?: return ESTADOS_ABERTA
            if (limpo.equals("ABERTA", ignoreCase = true)) {
                return ESTADOS_ABERTA
            }
            if (limpo.equals("TODOS", ignoreCase = true) || limpo.equals("ALL", ignoreCase = true)) {
                return null
            }
            return listOf(limpo)
        }

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
