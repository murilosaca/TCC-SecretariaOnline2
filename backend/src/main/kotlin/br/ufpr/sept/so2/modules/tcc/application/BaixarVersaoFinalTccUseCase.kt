package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BaixarVersaoFinalTccUseCase(
    private val obterTccUseCase: ObterTccUseCase,
) {
    @Transactional(readOnly = true)
    fun execute(id: UUID, usuarioId: UUID, authorities: List<String>): ArquivoTcc {
        val tcc = obterTccUseCase.execute(id, usuarioId, authorities)
        val bytes = tcc.conteudo ?: throw RecursoNaoEncontradoException("Arquivo do TCC não encontrado.")
        return ArquivoTcc(tcc.nomeArquivo ?: "tcc.pdf", bytes)
    }
}

class ArquivoTcc(val nome: String, val bytes: ByteArray)
