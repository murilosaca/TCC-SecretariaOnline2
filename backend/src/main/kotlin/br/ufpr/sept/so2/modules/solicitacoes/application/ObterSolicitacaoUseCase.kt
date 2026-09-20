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
        val deliberante = authorities.contains(AUTHORITY_DELIBERATE)
        val titular = dono && authorities.contains(AUTHORITY_VIEW_OWN)
        if (!titular && !deliberante) {
            throw RecursoNaoEncontradoException("Solicitação não encontrada.")
        }
        return solicitacao
    }

    companion object {
        const val AUTHORITY_VIEW_OWN = "request.view_own"
        const val AUTHORITY_DELIBERATE = "request.deliberate"
    }
}
