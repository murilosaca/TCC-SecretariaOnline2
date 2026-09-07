package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConsultarProtocoloPublicoUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
) {

    @Transactional(readOnly = true)
    fun execute(protocoloBruto: String): Solicitacao {
        val protocolo = Protocolo.of(protocoloBruto)
        return solicitacaoRepository.findByProtocolo(protocolo.valor)
            .orElseThrow {
                RecursoNaoEncontradoException(
                    "Nenhum protocolo registrado com este identificador.",
                )
            }
    }
}
