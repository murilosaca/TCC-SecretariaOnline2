package br.ufpr.sept.so2.modules.certificados.application

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.certificados.application.ports.BeneficiarioPort
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoRepository
import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoSigner
import br.ufpr.sept.so2.modules.certificados.application.ports.PdfCertificadoRenderer
import br.ufpr.sept.so2.modules.certificados.application.ports.PdfCertificadoRenderer.Dados
import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.UUID

class EmitirCertificadoPorFormativaUseCaseTest : StringSpec({
    val formativaId = UUID.fromString("01800000-0000-7000-8000-0000000000f1")
    val alunoId = UUID.fromString("01800000-0000-7000-8000-0000000000a1")
    val atorId = UUID.fromString("01800000-0000-7000-8000-0000000000c1")

    fun useCase(
        repo: CertificadoRepository,
        beneficiario: BeneficiarioPort = mockk(),
        renderer: PdfCertificadoRenderer = mockk(),
        signer: CertificadoSigner = mockk(),
        storage: ObjectStoragePort = mockk(relaxed = true),
        outbox: OutboxPort = mockk(relaxed = true),
        audit: AuditLogPort = mockk(relaxed = true),
    ) = EmitirCertificadoPorFormativaUseCase(
        repo,
        beneficiario,
        renderer,
        signer,
        storage,
        outbox,
        audit,
        ObjectMapper(),
        "http://localhost:5174",
    )

    "não duplica quando já existe certificado da formativa" {
        val repo = mockk<CertificadoRepository>()
        every { repo.findByIdFormativa(formativaId) } returns mockk<Certificado>()

        useCase(repo).emitirSeAusente(formativaId, alunoId, null, "Oficina", 4, atorId, null)

        verify(exactly = 0) { repo.save(any()) }
    }

    "emite pdf, assina, outbox e auditoria" {
        val repo = mockk<CertificadoRepository>()
        val beneficiario = mockk<BeneficiarioPort>()
        val renderer = mockk<PdfCertificadoRenderer>()
        val signer = mockk<CertificadoSigner>()
        val storage = mockk<ObjectStoragePort>()
        val outbox = mockk<OutboxPort>()
        val audit = mockk<AuditLogPort>()
        every { repo.findByIdFormativa(formativaId) } returns null
        every { beneficiario.nomeDe(alunoId) } returns "Aluno Dev"
        every { renderer.render(any<Dados>(), isNull()) } returns byteArrayOf(1, 2, 3, 4)
        every { renderer.render(any<Dados>(), match { it!!.contains("/publico/verificar-certificado/") }) } returns
            byteArrayOf(5, 6, 7, 8)
        every { signer.sign(any()) } returns "assinatura-base64"
        every { storage.putObject(any(), any(), any()) } just runs
        every { repo.save(any()) } answers { firstArg() }
        every { outbox.enqueue(eq("certificado.emitido"), any()) } just runs
        every { audit.append(eq("certificado.emitido"), eq(atorId), any(), eq("127.0.0.1")) } just runs

        useCase(repo, beneficiario, renderer, signer, storage, outbox, audit)
            .emitirSeAusente(formativaId, alunoId, null, "Oficina", 4, atorId, "127.0.0.1")

        verify { storage.putObject(match { it.startsWith("certificados/") }, "application/pdf", any()) }
        verify { repo.save(any()) }
        verify { outbox.enqueue("certificado.emitido", any()) }
        verify { audit.append("certificado.emitido", atorId, any(), "127.0.0.1") }
        verify { signer.sign(any()) }
    }
})
