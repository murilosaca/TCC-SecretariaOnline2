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
 * Fila compacta de AUTORIZACAO_IMAGEM. O estado ABERTA aqui é literal:
 * não usa o alias da fila central (EM_ANALISE / EM_AJUSTE).
 */
@Service
class ListarAutorizacoesImagemUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val solicitacaoCursoEscopo: SolicitacaoCursoEscopo,
) {
    @Transactional(readOnly = true)
    fun execute(atorId: UUID, estado: String?, pageable: Pageable): Page<Solicitacao> {
        val solicitantes = solicitacaoCursoEscopo.solicitanteIdsNoEscopo(atorId)
        val estados = if (estado.isNullOrBlank() || estado.equals("TODOS", ignoreCase = true)) {
            null
        } else {
            listOf(estado.trim())
        }
        val size = pageable.pageSize.coerceIn(1, 50)
        val ordenado = PageRequest.of(
            pageable.pageNumber.coerceAtLeast(0),
            size,
            Sort.by(Sort.Direction.DESC, "createdAt"),
        )
        return solicitacaoRepository.findFilaCurso(
            solicitantes,
            estados,
            TIPO,
            false,
            OffsetDateTime.now(),
            ordenado,
        )
    }

    companion object {
        const val TIPO: String = "AUTORIZACAO_IMAGEM"
    }
}
