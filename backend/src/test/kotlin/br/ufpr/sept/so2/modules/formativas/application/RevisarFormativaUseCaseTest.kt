package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoPorFormativaPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.UUID

class RevisarFormativaUseCaseTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-20T18:00:00Z")
    val formativaId = UUID.fromString("01800000-0000-7000-8000-0000000000f1")
    val revisorId = UUID.fromString("01800000-0000-7000-8000-0000000000c1")
    val objectMapper = ObjectMapper()

    fun aguardando(): Formativa {
        val formativa = Formativa.viaPresenca(
            formativaId,
            UUID.fromString("01800000-0000-7000-8000-0000000000a1"),
            UUID.fromString("01800000-0000-7000-8000-0000000000e1"),
            "Oficina",
            4,
            agora,
        )
        formativa.confirmar(agora.plusMinutes(1))
        return formativa
    }

    fun useCase(
        repo: FormativaRepository,
        outbox: OutboxPort = mockk(relaxed = true),
        audit: AuditLogPort = mockk(relaxed = true),
        certificado: CertificadoPorFormativaPort = mockk(relaxed = true),
    ) = RevisarFormativaUseCase(repo, outbox, audit, objectMapper, certificado)

    "aprovar publica outbox, auditoria e emite certificado na mesma execução" {
        val repo = mockk<FormativaRepository>()
        val outbox = mockk<OutboxPort>()
        val audit = mockk<AuditLogPort>()
        val certificado = mockk<CertificadoPorFormativaPort>(relaxed = true)
        every { repo.findById(formativaId) } returns aguardando()
        every { repo.save(any()) } answers { firstArg() }
        every { outbox.enqueue(eq("formativa.aprovada"), any()) } just runs
        every { audit.append(eq("formativa.aprovada"), eq(revisorId), any(), eq("127.0.0.1")) } just runs

        val result = useCase(repo, outbox, audit, certificado).execute(
            formativaId,
            revisorId,
            "APROVAR",
            "Presença validada; horas da oficina mantidas.",
            "127.0.0.1",
        )

        result.estado shouldBe FormativaEstado.APROVADA
        result.cargaHoraria shouldBe 4
        verify { outbox.enqueue("formativa.aprovada", any()) }
        verify { audit.append("formativa.aprovada", revisorId, any(), "127.0.0.1") }
        verify {
            certificado.emitirSeAusente(
                formativaId,
                result.idAluno,
                result.idEvento,
                "Oficina",
                4,
                revisorId,
                "127.0.0.1",
            )
        }
    }

    "indeferir não emite certificado" {
        val repo = mockk<FormativaRepository>()
        val certificado = mockk<CertificadoPorFormativaPort>(relaxed = true)
        every { repo.findById(formativaId) } returns aguardando()
        every { repo.save(any()) } answers { firstArg() }

        val result = useCase(repo, certificado = certificado).execute(
            formativaId,
            revisorId,
            "INDEFERIR",
            "Indeferimento fundamentado com mais de vinte caracteres.",
            null,
        )

        result.estado shouldBe FormativaEstado.INDEFERIDA
        verify(exactly = 0) { certificado.emitirSeAusente(any(), any(), any(), any(), any(), any(), any()) }
    }

    "indefer curto não persiste" {
        val repo = mockk<FormativaRepository>()
        every { repo.findById(formativaId) } returns aguardando()

        shouldThrow<DadoInvalidoException> {
            useCase(repo).execute(formativaId, revisorId, "INDEFERIR", "curto", null)
        }
        verify(exactly = 0) { repo.save(any()) }
    }
})
