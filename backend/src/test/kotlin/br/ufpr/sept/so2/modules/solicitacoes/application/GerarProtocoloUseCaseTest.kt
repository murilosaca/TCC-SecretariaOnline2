package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.iam.application.IamFakes
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.PdfProtocoloRenderer
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.AnexoPdf
import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

class GerarProtocoloUseCaseTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-20T15:00:00Z")
    val alunoId = UUID.fromString("01800000-0000-7000-8000-0000000000cc")
    val parser = WorkflowJsonParser(ObjectMapper())
    val pdf = "%PDF-protocolo".toByteArray()

    fun deliberada(): Solicitacao {
        val tipo = DeclaracaoSimplesSeed.tipo(agora)
        val aberta = Solicitacao.abrir(
            UUID.fromString("01800000-0000-7000-8000-0000000000aa"),
            UUID.randomUUID(),
            tipo,
            alunoId,
            Protocolo.formatar(2026, 11),
            """{"finalidade":"vinculo academico"}""",
            parser.parse(tipo.workflowJson),
            agora,
        )
        aberta.transicionar(UUID.randomUUID(), parser.parse(tipo.workflowJson), "DEFER", alunoId, "ok", agora)
        return aberta
    }

    "gera pdf com hash do proprio arquivo e nao repete a emissao" {
        val repo = mockk<SolicitacaoRepository>()
        val storage = mockk<ObjectStoragePort>()
        val renderer = mockk<PdfProtocoloRenderer>()
        val solicitacao = deliberada()
        val guardados = linkedMapOf<String, ByteArray>()
        every { repo.findById(solicitacao.id) } returns Optional.of(solicitacao)
        every { repo.save(any()) } answers { firstArg() }
        every { renderer.render(any()) } returns pdf
        every { storage.putObject(any(), any(), any()) } answers {
            guardados[firstArg()] = thirdArg()
        }
        every { storage.exists(any()) } answers { guardados.containsKey(firstArg()) }
        every { storage.presignGetUrl(any(), any(), any()) } answers { "https://minio/${firstArg<String>()}" }
        val caso = GerarProtocoloUseCase(repo, renderer, storage, ObjectMapper(), IamFakes.Settings())

        val primeiro = caso.execute(solicitacao.id, alunoId)
        primeiro.hashSha256 shouldBe AnexoPdf.sha256(pdf)
        primeiro.downloadUrl shouldContain "solicitacoes/"
        guardados.values.single().contentEquals(pdf) shouldBe true

        val segundo = caso.execute(solicitacao.id, alunoId)
        segundo.hashSha256 shouldBe primeiro.hashSha256
        verify(exactly = 1) { renderer.render(any()) }
        verify(exactly = 1) { storage.putObject(any(), "application/pdf", pdf) }
        verify { storage.presignGetUrl(any(), Duration.ofMinutes(15), any()) }
    }

    "antes da deliberacao nao grava pdf" {
        val repo = mockk<SolicitacaoRepository>()
        val tipo = DeclaracaoSimplesSeed.tipo(agora)
        val aberta = Solicitacao.abrir(
            UUID.randomUUID(),
            UUID.randomUUID(),
            tipo,
            alunoId,
            Protocolo.formatar(2026, 12),
            "{}",
            parser.parse(tipo.workflowJson),
            agora,
        )
        every { repo.findById(aberta.id) } returns Optional.of(aberta)
        val storage = mockk<ObjectStoragePort>(relaxed = true)
        shouldThrow<ConflitoEstadoException> {
            GerarProtocoloUseCase(
                repo,
                mockk(relaxed = true),
                storage,
                ObjectMapper(),
                IamFakes.Settings(),
            ).execute(aberta.id, alunoId)
        }
        verify(exactly = 0) { storage.putObject(any(), any(), any()) }
        verify(exactly = 0) { repo.save(any()) }
    }
})
