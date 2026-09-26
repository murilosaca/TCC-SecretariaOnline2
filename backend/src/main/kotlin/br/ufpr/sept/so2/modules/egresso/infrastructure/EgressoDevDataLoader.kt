package br.ufpr.sept.so2.modules.egresso.infrastructure

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.arquivos.domain.StorageKey
import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoRepository
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoSigner
import br.ufpr.sept.so2.modules.certificados.application.ports.PdfCertificadoRenderer
import br.ufpr.sept.so2.modules.certificados.application.ports.PdfCertificadoRenderer.Dados
import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import br.ufpr.sept.so2.modules.certificados.domain.CertificadoTipo
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.HexFormat
import java.util.UUID

/**
 * Emissão original do certificado de desenvolvimento. A reemissão do portal não passa por aqui:
 * ela só gera URL pré-assinada do artefato já gravado (mesmo hash/assinatura).
 */
@Component
@Profile("dev")
@Order(22)
class EgressoDevDataLoader(
    private val properties: IamProperties,
    private val alunoRepository: AlunoRepository,
    private val certificadoRepository: CertificadoRepository,
    private val renderer: PdfCertificadoRenderer,
    private val signer: CertificadoSigner,
    private val objectStoragePort: ObjectStoragePort,
    @Value("\${app.iam.frontend-base-url:http://localhost:5174}")
    private val frontendBaseUrl: String,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (!properties.seed.enabled) {
            return
        }
        val aluno = alunoRepository.findByGrr(GRR).orElse(null)
        if (aluno == null) {
            LOG.warn("Seed de certificado do egresso ignorado: cadastro acadêmico ausente.")
            return
        }
        val jaExiste = certificadoRepository
            .findByAluno(aluno.id, PageRequest.of(0, 20))
            .content
            .any { it.titulo == TITULO }
        if (jaExiste) {
            return
        }
        emitir(aluno.id, nome(aluno.nomeSocial, aluno.nome))
        LOG.info("Certificado de desenvolvimento do egresso pronto.")
    }

    private fun emitir(alunoId: UUID, nome: String) {
        val agora = OffsetDateTime.now()
        val id = Uuids.v7()
        val dados = Dados(nome, TITULO, CARGA, agora, null)
        val canonico = renderer.render(dados, null)
        val hashBytes = MessageDigest.getInstance("SHA-256").digest(canonico)
        val hashHex = HEX.formatHex(hashBytes)
        val qr = "${frontendBaseUrl.trimEnd('/')}/publico/verificar-certificado/$hashHex"
        val pdf = renderer.render(dados.copy(hashSha256 = hashHex), qr)
        val assinatura = signer.sign(hashBytes)
        val storageKey = StorageKey.certificado(id).value
        objectStoragePort.putObject(storageKey, "application/pdf", pdf)
        certificadoRepository.save(
            Certificado(
                id,
                alunoId,
                null,
                null,
                CertificadoTipo.EVENTO,
                TITULO,
                CARGA,
                nome,
                hashHex,
                assinatura,
                storageKey,
                agora,
                agora,
                agora,
            ),
        )
    }

    companion object {
        private const val GRR = "GRR20240006"
        private const val TITULO = "Seminário de extensão SEPT"
        private const val CARGA = 8
        private val HEX = HexFormat.of()
        private val LOG = LoggerFactory.getLogger(EgressoDevDataLoader::class.java)

        private fun nome(nomeSocial: String?, nome: String): String {
            val social = nomeSocial?.trim().orEmpty()
            if (social.isNotEmpty()) {
                return social
            }
            return nome.trim().ifEmpty { "Egresso Dev" }
        }
    }
}
