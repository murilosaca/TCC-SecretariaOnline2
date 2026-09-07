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
) {

    @Transactional(readOnly = true)
    fun execute(id: UUID, solicitanteId: UUID): Solicitacao {
        val solicitacao = solicitacaoRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Solicitação não encontrada.") }
        if (!solicitacao.pertenceA(solicitanteId)) {
            throw RecursoNaoEncontradoException("Solicitação não encontrada.")
        }
        return solicitacao
    }
}
