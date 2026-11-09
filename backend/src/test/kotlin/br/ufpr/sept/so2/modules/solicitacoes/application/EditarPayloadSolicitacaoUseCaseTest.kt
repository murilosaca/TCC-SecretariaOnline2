package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
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
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

class EditarPayloadSolicitacaoUseCaseTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-20T15:00:00Z")
    val alunoId = UUID.fromString("01800000-0000-7000-8000-0000000000cc")
    val mapper = ObjectMapper()
    val parser = WorkflowJsonParser(mapper)
    val validador = FormSchemaValidator(mapper)

    fun emAjuste(): Solicitacao {
        val tipo = DeclaracaoSimplesSeed.tipo(agora)
        val aberta = Solicitacao.abrir(
            UUID.fromString("01800000-0000-7000-8000-0000000000aa"),
            UUID.randomUUID(),
            tipo,
            alunoId,
            Protocolo.formatar(2026, 8),
            """{"finalidade":"vinculo academico"}""",
            parser.parse(tipo.workflowJson),
            agora,
        )
        aberta.transicionar(UUID.randomUUID(), parser.parse(tipo.workflowJson), "REQUEST_ADJUST", alunoId, "ajuste", agora)
        return aberta
    }

    "em ajuste grava o payload e o audit fica sem o formulario" {
        val repo = mockk<SolicitacaoRepository>()
        val audit = mockk<AuditLogPort>()
        val solicitacao = emAjuste()
        val evento = slot<String>()
        every { repo.findById(solicitacao.id) } returns Optional.of(solicitacao)
        every { repo.save(any()) } answers { firstArg() }
        every { audit.append("solicitacao.payload_atualizado", alunoId, capture(evento), any()) } just runs

        val salva = EditarPayloadSolicitacaoUseCase(repo, parser, validador, audit, mapper)
            .execute(solicitacao.id, alunoId, mapOf("finalidade" to "estagio obrigatorio"), "127.0.0.1")

        salva.payloadJson.shouldContain("estagio obrigatorio")
        evento.captured.shouldNotContain("finalidade")
        evento.captured.shouldNotContain("estagio")
        evento.captured.shouldContain(solicitacao.protocolo.valor)
    }

    "fora de ajuste, payload invalido ou outro aluno nao grava" {
        val repo = mockk<SolicitacaoRepository>()
        val tipo = DeclaracaoSimplesSeed.tipo(agora)
        val aberta = Solicitacao.abrir(
            UUID.randomUUID(),
            UUID.randomUUID(),
            tipo,
            alunoId,
            Protocolo.formatar(2026, 9),
            """{"finalidade":"vinculo academico"}""",
            parser.parse(tipo.workflowJson),
            agora,
        )
        val ajuste = emAjuste()
        every { repo.findById(aberta.id) } returns Optional.of(aberta)
        every { repo.findById(ajuste.id) } returns Optional.of(ajuste)
        val caso = EditarPayloadSolicitacaoUseCase(repo, parser, validador, mockk(relaxed = true), mapper)
        shouldThrow<ConflitoEstadoException> {
            caso.execute(aberta.id, alunoId, mapOf("finalidade" to "vinculo academico"), null)
        }
        shouldThrow<DadoInvalidoException> {
            caso.execute(ajuste.id, alunoId, mapOf("finalidade" to "ok"), null)
        }
        shouldThrow<RecursoNaoEncontradoException> {
            caso.execute(ajuste.id, UUID.randomUUID(), mapOf("finalidade" to "estagio obrigatorio"), null)
        }
        verify(exactly = 0) { repo.save(any()) }
    }
})
