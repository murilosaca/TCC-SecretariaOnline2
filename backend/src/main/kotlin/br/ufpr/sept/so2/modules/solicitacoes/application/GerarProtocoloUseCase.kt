package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.iam.application.IamSettings
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.PdfProtocoloRenderer
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.AnexoPdf
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID

@Service
class GerarProtocoloUseCase(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val renderer: PdfProtocoloRenderer,
    private val objectStoragePort: ObjectStoragePort,
    private val objectMapper: ObjectMapper,
    private val iamSettings: IamSettings,
) {
    @Transactional
    fun execute(solicitacaoId: UUID, atorId: UUID): ProtocoloEmitido {
        val solicitacao = solicitacaoRepository.findById(solicitacaoId)
            .orElseThrow { RecursoNaoEncontradoException("Solicitação não encontrada.") }
        if (!solicitacao.pertenceA(atorId)) {
            throw RecursoNaoEncontradoException("Solicitação não encontrada.")
        }
        if (solicitacao.estado != ESTADO) {
            throw ConflitoEstadoException("O protocolo PDF só é emitido depois da deliberação.")
        }
        val key = solicitacao.protocoloStorageKey
        val hash = solicitacao.hashSha256
        if (!key.isNullOrBlank() && !hash.isNullOrBlank() && objectStoragePort.exists(key)) {
            return ProtocoloEmitido(url(key, solicitacao), hash)
        }
        val urlPublica = iamSettings.frontendBaseUrl.trimEnd('/') +
            "/publico/verificar-protocolo/" + solicitacao.protocolo.valor
        val pdf = renderer.render(
            PdfProtocoloRenderer.Dados(
                solicitacao.protocolo.valor,
                solicitacao.tipoNome,
                solicitacao.estado,
                campos(solicitacao),
                solicitacao.eventos.map { evento ->
                    val de = evento.estadoDe?.let { "$it → " }.orEmpty()
                    "$de${evento.estadoPara} (${evento.tipo})"
                },
                urlPublica,
            ),
        )
        val novoHash = AnexoPdf.sha256(pdf)
        val storageKey = "solicitacoes/${solicitacao.id}/protocolo.pdf"
        objectStoragePort.putObject(storageKey, "application/pdf", pdf)
        solicitacao.registrarProtocolo(novoHash, storageKey, OffsetDateTime.now())
        solicitacaoRepository.save(solicitacao)
        return ProtocoloEmitido(url(storageKey, solicitacao), novoHash)
    }

    private fun url(key: String, solicitacao: Solicitacao): String =
        objectStoragePort.presignGetUrl(key, Duration.ofMinutes(15), "${solicitacao.protocolo.valor}.pdf")

    private fun campos(solicitacao: Solicitacao): List<PdfProtocoloRenderer.Par> {
        val payload = readMap(solicitacao.payloadJson)
        if (payload.isEmpty()) {
            return emptyList()
        }
        return payload.map { (chave, valor) ->
            PdfProtocoloRenderer.Par(chave, valor?.toString().orEmpty())
        }
    }

    private fun readMap(json: String?): Map<String, Any?> {
        if (json.isNullOrBlank()) {
            return emptyMap()
        }
        return try {
            objectMapper.readValue(json, object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any?>>() {})
        } catch (_: Exception) {
            emptyMap()
        }
    }

    data class ProtocoloEmitido(val downloadUrl: String, val hashSha256: String)

    companion object {
        const val ESTADO: String = "DELIBERADA"
    }
}
