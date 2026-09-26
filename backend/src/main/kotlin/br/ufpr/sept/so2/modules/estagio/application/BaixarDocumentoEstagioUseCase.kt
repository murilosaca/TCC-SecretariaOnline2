package br.ufpr.sept.so2.modules.estagio.application

import br.ufpr.sept.so2.modules.arquivos.application.PresignDownloadUseCase
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BaixarDocumentoEstagioUseCase(
    private val obterEstagioUseCase: ObterEstagioUseCase,
    private val presignDownloadUseCase: PresignDownloadUseCase,
) {
    @Transactional(readOnly = true)
    fun execute(
        estagioId: UUID,
        documentoId: UUID,
        usuarioId: UUID,
        authorities: List<String>,
    ): ArquivoEstagioPresignado {
        val estagio = obterEstagioUseCase.execute(estagioId, usuarioId, authorities)
        val documento = estagio.documentos.find { it.id == documentoId }
            ?: throw RecursoNaoEncontradoException("Documento de estágio não encontrado.")
        val key = documento.storageKey
            ?: throw RecursoNaoEncontradoException("Arquivo do documento não encontrado.")
        val nome = documento.nomeArquivo ?: "documento.pdf"
        val url = presignDownloadUseCase.execute(key, nome)
        return ArquivoEstagioPresignado(nome, url, presignDownloadUseCase.ttlSeconds())
    }
}

data class ArquivoEstagioPresignado(
    val nome: String,
    val downloadUrl: String,
    val expiresInSeconds: Long,
)
