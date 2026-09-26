package br.ufpr.sept.so2.modules.tcc.application

import br.ufpr.sept.so2.modules.arquivos.application.PresignDownloadUseCase
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BaixarVersaoFinalTccUseCase(
    private val obterTccUseCase: ObterTccUseCase,
    private val presignDownloadUseCase: PresignDownloadUseCase,
) {
    @Transactional(readOnly = true)
    fun execute(id: UUID, usuarioId: UUID, authorities: List<String>): ArquivoTccPresignado {
        val tcc = obterTccUseCase.execute(id, usuarioId, authorities)
        val key = tcc.storageKey ?: throw RecursoNaoEncontradoException("Arquivo do TCC não encontrado.")
        val url = presignDownloadUseCase.execute(key, tcc.nomeArquivo ?: "tcc.pdf")
        return ArquivoTccPresignado(
            tcc.nomeArquivo ?: "tcc.pdf",
            url,
            presignDownloadUseCase.ttlSeconds(),
        )
    }
}

data class ArquivoTccPresignado(
    val nome: String,
    val downloadUrl: String,
    val expiresInSeconds: Long,
)
