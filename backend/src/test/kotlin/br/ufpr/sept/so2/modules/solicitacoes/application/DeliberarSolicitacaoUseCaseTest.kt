package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
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
import java.util.Optional
import java.util.UUID

class DeliberarSolicitacaoUseCaseTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-20T15:00:00Z")
    val solicitacaoId = UUID.fromString("01800000-0000-7000-8000-0000000000aa")
    val atorId = UUID.fromString("01800000-0000-7000-8000-0000000000dd")
    val objectMapper = ObjectMapper()
    val parser = WorkflowJsonParser(objectMapper)

    fun aberta(): Solicitacao {
        val tipo = DeclaracaoSimplesSeed.tipo(agora)
        return Solicitacao.abrir(
            solicitacaoId,
            UUID.fromString("01800000-0000-7000-8000-0000000000bb"),
            tipo,
            UUID.fromString("01800000-0000-7000-8000-0000000000cc"),
            Protocolo.formatar(2026, 9),
            "{\"finalidade\":\"vinculo academico\"}",
            parser.parse(tipo.workflowJson),
            agora,
        )
    }

    fun useCase(
        repo: SolicitacaoRepository,
        outbox: OutboxPort = mockk(relaxed = true),
        audit: AuditLogPort = mockk(relaxed = true),
    ) = DeliberarSolicitacaoUseCase(repo, parser, outbox, audit, objectMapper)

    "defer publica outbox e muda estado" {
        val repo = mockk<SolicitacaoRepository>()
        val outbox = mockk<OutboxPort>()
        val audit = mockk<AuditLogPort>()
        val solicitacao = aberta()
        every { repo.findById(solicitacaoId) } returns Optional.of(solicitacao)
        every { repo.save(any()) } answers { firstArg() }
        every { outbox.enqueue(eq("solicitacao.deliberada"), any()) } just runs
        every { audit.append(eq("solicitacao.deliberada"), eq(atorId), any(), eq("127.0.0.1")) } just runs

        val result = useCase(repo, outbox, audit).execute(
            solicitacaoId,
            atorId,
            "DEFER",
            "Deferido para fins de estágio.",
            "127.0.0.1",
        )

        result.estado shouldBe "DELIBERADA"
        verify { outbox.enqueue("solicitacao.deliberada", any()) }
        verify { audit.append("solicitacao.deliberada", atorId, any(), "127.0.0.1") }
    }

    "indefer curto e CLOSE sao rejeitados sem persistir" {
        val repo = mockk<SolicitacaoRepository>()
        every { repo.findById(solicitacaoId) } returns Optional.of(aberta())

        shouldThrow<DadoInvalidoException> {
            useCase(repo).execute(solicitacaoId, atorId, "INDEFER", "curto", null)
        }
        shouldThrow<DadoInvalidoException> {
            useCase(repo).execute(solicitacaoId, atorId, "CLOSE", "Parecer suficiente.", null)
        }
        verify(exactly = 0) { repo.save(any()) }
    }

    "transicao ilegal e ausencia retornam conflito ou 404 de dominio" {
        val repo = mockk<SolicitacaoRepository>()
        val jaDeliberada = aberta()
        jaDeliberada.transicionar(
            UUID.fromString("01800000-0000-7000-8000-0000000000ee"),
            parser.parse(DeclaracaoSimplesSeed.WORKFLOW_JSON),
            "DEFER",
            atorId,
            "ok",
            agora,
        )
        every { repo.findById(solicitacaoId) } returns Optional.of(jaDeliberada)
        shouldThrow<ConflitoEstadoException> {
            useCase(repo).execute(solicitacaoId, atorId, "DEFER", "Segundo parecer.", null)
        }

        every { repo.findById(solicitacaoId) } returns Optional.empty()
        shouldThrow<RecursoNaoEncontradoException> {
            useCase(repo).execute(solicitacaoId, atorId, "DEFER", "Parecer.", null)
        }
    }
})
