package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ObterSolicitacaoUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val validarTokenDeliberacaoUseCase: ValidarTokenDeliberacaoUseCase,
    private val solicitacaoCursoEscopo: SolicitacaoCursoEscopo,
) {

    @Transactional(readOnly = true)
    fun execute(
        id: UUID,
        usuarioId: UUID,
        authorities: List<String>,
        deepLinkToken: String? = null,
    ): Solicitacao {
        validarTokenDeliberacaoUseCase.execute(deepLinkToken, id, usuarioId)
        val solicitacao = solicitacaoRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Solicitação não encontrada.") }
        val dono = solicitacao.pertenceA(usuarioId)
        val titular = dono && authorities.contains(AUTHORITY_VIEW_OWN)
        if (titular) {
            return solicitacao
        }
        if (authorities.contains(AUTHORITY_VIEW_CURSO)) {
            if (solicitacaoCursoEscopo.solicitanteNoEscopo(usuarioId, solicitacao.solicitanteId)) {
                return solicitacao
            }
            throw RecursoNaoEncontradoException("Solicitação não encontrada.")
        }
        if (authorities.contains(AUTHORITY_DELIBERATE)) {
            return solicitacao
        }
        throw RecursoNaoEncontradoException("Solicitação não encontrada.")
    }

    companion object {
        const val AUTHORITY_VIEW_OWN = "request.view_own"
        const val AUTHORITY_DELIBERATE = "request.deliberate"
        const val AUTHORITY_VIEW_CURSO = "request.view_curso"
    }
}
