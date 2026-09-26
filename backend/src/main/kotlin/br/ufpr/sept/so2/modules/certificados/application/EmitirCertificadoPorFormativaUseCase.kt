package br.ufpr.sept.so2.modules.certificados.application

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.arquivos.domain.StorageKey
import br.ufpr.sept.so2.modules.certificados.application.ports.BeneficiarioPort
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoPorFormativaPort
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoRepository
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoSigner
import br.ufpr.sept.so2.modules.certificados.application.ports.PdfCertificadoRenderer
import br.ufpr.sept.so2.modules.certificados.application.ports.PdfCertificadoRenderer.Dados
import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.HexFormat
import java.util.UUID

@Service
class EmitirCertificadoPorFormativaUseCase(
    private val certificadoRepository: CertificadoRepository,
    private val beneficiarioPort: BeneficiarioPort,
    private val renderer: PdfCertificadoRenderer,
    private val signer: CertificadoSigner,
    private val objectStoragePort: ObjectStoragePort,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val objectMapper: ObjectMapper,
    @Value("\${app.iam.frontend-base-url:http://localhost:5174}")
    private val frontendBaseUrl: String,
) : CertificadoPorFormativaPort {

    @Transactional
    override fun emitirSeAusente(
        formativaId: UUID,
        alunoId: UUID,
        eventoId: UUID?,
        titulo: String,
        cargaHoraria: Int,
        atorId: UUID,
        ip: String?,
    ) {
        val existente = certificadoRepository.findByIdFormativa(formativaId)
        if (existente != null) {
            return
        }
        val nome = beneficiarioPort.nomeDe(alunoId)
            ?: throw DadoInvalidoException("Não foi possível identificar o beneficiário do certificado.")
        val agora = OffsetDateTime.now()
        val id = Uuids.v7()
        val dados = Dados(nome, titulo, cargaHoraria, agora, null)
        val pdfCanonico = renderer.render(dados, null)
        val hashBytes = sha256(pdfCanonico)
        val hashHex = HEX.formatHex(hashBytes)
        val qrUrl = "${frontendBaseUrl.trimEnd('/')}/publico/verificar-certificado/$hashHex"
        val pdf = renderer.render(dados.copy(hashSha256 = hashHex), qrUrl)
        val assinatura = signer.sign(hashBytes)
        val storageKey = StorageKey.certificado(id).value
        objectStoragePort.putObject(storageKey, "application/pdf", pdf)
        val certificado = Certificado.deFormativa(
            id,
            alunoId,
            formativaId,
            eventoId,
            titulo,
            cargaHoraria,
            nome,
            hashHex,
            assinatura,
            storageKey,
            agora,
        )
        try {
            certificadoRepository.save(certificado)
        } catch (_: DataIntegrityViolationException) {
            LOG.info("Certificado já existia para a formativa {}; emissão idempotente.", formativaId)
            return
        }
        val payload = toJson(
            mapOf(
                "certificadoId" to certificado.id.toString(),
                "formativaId" to formativaId.toString(),
                "alunoId" to alunoId.toString(),
                "hashSha256" to hashHex,
                "cargaHoraria" to cargaHoraria,
                "storageKey" to storageKey,
            ),
        )
        outboxPort.enqueue(TIPO, payload)
        auditLogPort.append(TIPO, atorId, payload, ip)
    }

    private fun toJson(valor: Any): String =
        try {
            objectMapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível serializar o evento de certificado.")
        }

    companion object {
        const val TIPO = "certificado.emitido"
        private val LOG = LoggerFactory.getLogger(EmitirCertificadoPorFormativaUseCase::class.java)
        private val HEX = HexFormat.of()

        fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)
    }
}
