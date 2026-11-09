package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.OffsetDateTime
import java.util.UUID

class DeliberarSolicitacaoLoteUseCaseTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-20T15:00:00Z")
    val atorId = UUID.fromString("01800000-0000-7000-8000-0000000000dd")
    val parser = WorkflowJsonParser(ObjectMapper())

    fun abrir(id: UUID, codigo: String, workflow: String, numero: Int): Solicitacao {
        val tipo = TipoSolicitacao(
            UUID.fromString("018f0000-0000-7000-8000-000000000099"),
            codigo,
            codigo,
            null,
            TipoSolicitacao.PUBLISHED,
            "{}",
            workflow,
            15,
            1,
            agora,
            agora,
        )
        return Solicitacao.abrir(
            id,
            UUID.randomUUID(),
            tipo,
            UUID.randomUUID(),
            Protocolo.formatar(2026, numero),
            """{"finalidade":"vinculo"}""",
            parser.parse(workflow),
            agora,
        )
    }

    fun caso(repo: SolicitacaoRepository) = DeliberarSolicitacaoLoteUseCase(
        repo,
        parser,
        mockk<OutboxPort>(relaxed = true),
        mockk<AuditLogPort>(relaxed = true),
        ObjectMapper(),
    )

    "tipo sem batch nao grava nenhuma deliberacao" {
        val repo = mockk<SolicitacaoRepository>()
        val um = abrir(UUID.randomUUID(), "DECLARACAO_SIMPLES", fluxo(false), 1)
        val dois = abrir(UUID.randomUUID(), "DECLARACAO_SIMPLES", fluxo(false), 2)
        every { repo.findByIdsForUpdate(any()) } returns listOf(um, dois)

        shouldThrow<DadoInvalidoException> {
            caso(repo).execute(atorId, listOf(um.id, dois.id), "DEFER", "Parecer suficiente.", null)
        }

        um.estado shouldBe "EM_ANALISE"
        verify(exactly = 0) { repo.save(any()) }
    }

    "autorizacao de imagem nao entra neste lote" {
        val repo = mockk<SolicitacaoRepository>()
        val imagem = abrir(UUID.randomUUID(), "AUTORIZACAO_IMAGEM", fluxo(true), 3)
        every { repo.findByIdsForUpdate(any()) } returns listOf(imagem)

        shouldThrow<DadoInvalidoException> {
            caso(repo).execute(atorId, listOf(imagem.id), "DEFER", "Parecer suficiente.", null)
        }
        verify(exactly = 0) { repo.save(any()) }
    }

    "lote permitido delibera todos ou nenhum" {
        val repo = mockk<SolicitacaoRepository>()
        val um = abrir(UUID.randomUUID(), "LOTE_OK", fluxo(true), 4)
        val fechada = abrir(UUID.randomUUID(), "LOTE_OK", fluxo(true), 5)
        fechada.transicionar(UUID.randomUUID(), parser.parse(fluxo(true)), "DEFER", atorId, "ja", agora)
        every { repo.findByIdsForUpdate(any()) } returns listOf(um, fechada)

        shouldThrow<ConflitoEstadoException> {
            caso(repo).execute(atorId, listOf(um.id, fechada.id), "DEFER", "Parecer suficiente.", null)
        }
        verify(exactly = 0) { repo.save(any()) }

        val outra = abrir(UUID.randomUUID(), "LOTE_OK", fluxo(true), 6)
        every { repo.findByIdsForUpdate(any()) } returns listOf(um, outra)
        every { repo.save(any()) } answers { firstArg() }
        val salvas = caso(repo).execute(atorId, listOf(um.id, outra.id), "DEFER", "Parecer suficiente.", null)
        salvas.map { it.estado } shouldBe listOf("DELIBERADA", "DELIBERADA")
    }
})

private fun fluxo(batch: Boolean) = """
    {
      "initial": "EM_ANALISE",
      "batch": $batch,
      "states": {
        "EM_ANALISE": { "on": { "DEFER": "DELIBERADA", "INDEFER": "INDEFERIDA" } },
        "DELIBERADA": { "on": {} },
        "INDEFERIDA": { "on": {} }
      }
    }
""".trimIndent()
