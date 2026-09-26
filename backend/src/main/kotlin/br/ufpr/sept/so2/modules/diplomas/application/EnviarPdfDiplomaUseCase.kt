package br.ufpr.sept.so2.modules.diplomas.application

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.arquivos.domain.StorageKey
import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class EnviarPdfDiplomaUseCase(
    private val diplomaRepository: DiplomaRepository,
    private val objectStoragePort: ObjectStoragePort,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(
        diplomaId: UUID,
        nomeArquivo: String?,
        contentType: String?,
        bytes: ByteArray,
        atorId: UUID,
        ip: String?,
    ): Diploma {
        if (bytes.isEmpty()) {
            throw DadoInvalidoException("PDF do diploma é obrigatório.")
        }
        if (contentType != null && contentType.isNotBlank() && !contentType.contains("pdf", ignoreCase = true)) {
            throw DadoInvalidoException("Somente PDF é aceito para o diploma oficial.")
        }
        val diploma = diplomaRepository.findById(diplomaId)
            ?: throw RecursoNaoEncontradoException("Diploma não encontrado.")
        val storageKey = StorageKey.diploma(diploma.id).value
        DiplomaArquivo.nomeSeguro(nomeArquivo)
        objectStoragePort.putObject(storageKey, "application/pdf", bytes)
        diploma.anexarPdf(storageKey, OffsetDateTime.now())
        val salvo = diplomaRepository.save(diploma)
        DiplomaTrilha.registrar(
            outboxPort,
            auditLogPort,
            objectMapper,
            TIPO,
            atorId,
            salvo,
            ip,
            mapOf("storageKey" to storageKey),
        )
        return salvo
    }

    companion object {
        const val TIPO = "diploma.pdf_uploaded"
    }
}
