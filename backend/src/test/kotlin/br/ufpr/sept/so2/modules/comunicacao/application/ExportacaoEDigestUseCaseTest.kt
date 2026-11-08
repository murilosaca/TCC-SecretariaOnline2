package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoEntregaRepository
import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoRepository
import br.ufpr.sept.so2.modules.comunicacao.application.ports.EmailUsuarioPort
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailPort
import br.ufpr.sept.so2.modules.comunicacao.domain.Comunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.ComunicacaoEntrega
import br.ufpr.sept.so2.modules.comunicacao.domain.PrioridadeComunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.TipoAudiencia
import br.ufpr.sept.so2.modules.comunicacao.domain.TipoComunicacao
import br.ufpr.sept.so2.modules.comunicacao.infrastructure.ComunicacaoProperties
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.NotificacaoPreferenciaRepository
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.domain.ModoDigest
import br.ufpr.sept.so2.modules.iam.domain.NotificacaoPreferencia
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

class ExportacaoEDigestUseCaseTest : StringSpec({
    "exportacao sem template fica FAILED e não SENT" {
        val eventoId = UUID.fromString("01800000-0000-7000-8000-0000000000e1")
        val operador = UUID.fromString("01800000-0000-7000-8000-0000000000a1")
        val outbox = mockk<OutboxPort>(relaxed = true)
        val render = mockk<RenderizadorTemplatePort>()
        val claim = OutboxClaim(eventoId, "exportacao.pronta", """{"operadorId":"$operador"}""", 0)
        every { outbox.claimPending(any(), any()) } returns listOf(claim)
        every { render.renderizar("exportacao.pronta", any()) } returns null
        val despacho = DespacharOutboxUseCase(
            outbox,
            listOf(ExportacaoProntaOutboxHandler(mockk(), mockk(relaxed = true), ObjectMapper())),
            mockk<AuditLogPort>(relaxed = true),
            ComunicacaoProperties(),
            render,
        )

        despacho.execute()

        verify { outbox.markFailed(eventoId, 1, "Template exportacao.pronta ausente.") }
        verify(exactly = 0) { outbox.markSent(any()) }
    }

    "exportacao com template envia o e-mail do operador" {
        val eventoId = UUID.fromString("01800000-0000-7000-8000-0000000000e2")
        val operador = UUID.fromString("01800000-0000-7000-8000-0000000000a2")
        val outbox = mockk<OutboxPort>(relaxed = true)
        val render = mockk<RenderizadorTemplatePort>()
        val mail = mockk<MailPort>(relaxed = true)
        val emails = mockk<EmailUsuarioPort>()
        val claim = OutboxClaim(eventoId, "exportacao.pronta", """{"operadorId":"$operador"}""", 0)
        every { outbox.claimPending(any(), any()) } returns listOf(claim)
        every { render.renderizar(any(), any()) } returns MensagemTemplate("Exportação pronta", "O arquivo está disponível.")
        every { emails.emailDe(operador) } returns "secretaria.dev@ufpr.br"
        val despacho = DespacharOutboxUseCase(
            outbox,
            listOf(ExportacaoProntaOutboxHandler(emails, mail, ObjectMapper())),
            mockk<AuditLogPort>(relaxed = true),
            ComunicacaoProperties(),
            render,
        )

        despacho.execute()

        verify { mail.send(match { it.to == "secretaria.dev@ufpr.br" && it.subject == "Exportação pronta" }) }
        verify { outbox.markSent(eventoId) }
    }

    "digest em RESUMO sai pelo Outbox e DND segura o envio" {
        val usuario = UUID.fromString("01800000-0000-7000-8000-0000000000a3")
        val entregaId = UUID.fromString("01800000-0000-7000-8000-0000000000d1")
        val comunicacaoId = UUID.fromString("01800000-0000-7000-8000-0000000000c9")
        val agora = OffsetDateTime.parse("2026-11-08T15:00:00Z")
        val entrega = ComunicacaoEntrega(entregaId, comunicacaoId, usuario, null, null, true, false, agora, true)
        val comunicado = Comunicacao(
            comunicacaoId,
            TipoComunicacao.INSTITUCIONAL,
            "Banca marcada",
            "corpo",
            PrioridadeComunicacao.MEDIUM,
            usuario,
            TipoAudiencia.CURSO,
            UUID.fromString("01800000-0000-7000-8000-0000000000aa"),
            null,
            agora,
        )
        val preferencia = NotificacaoPreferencia(
            usuario,
            emptyMap(),
            LocalTime.of(22, 0),
            LocalTime.of(7, 0),
            ModoDigest.RESUMO,
            agora,
            agora,
        )
        val entregas = mockk<ComunicacaoEntregaRepository>()
        val comunicacoes = mockk<ComunicacaoRepository>()
        val preferencias = mockk<NotificacaoPreferenciaRepository>()
        val outbox = mockk<OutboxPort>(relaxed = true)
        val payload = slot<String>()
        every { entregas.findEmailAdiado() } returns listOf(entrega)
        every { entregas.save(any()) } answers { firstArg() }
        every { comunicacoes.findByIds(any()) } returns listOf(comunicado)
        every { preferencias.findByUsuarioId(usuario) } returns Optional.of(preferencia)
        every { outbox.enqueue(EnfileirarDigestUseCase.TIPO, capture(payload)) } returns Unit
        val useCase = EnfileirarDigestUseCase(entregas, comunicacoes, preferencias, outbox, ObjectMapper())

        useCase.execute(LocalTime.of(12, 0)) shouldBe 1
        payload.captured.contains("Banca marcada") shouldBe true
        entrega.emailAdiado shouldBe false

        entrega.emailAdiado = true
        useCase.execute(LocalTime.of(23, 0)) shouldBe 0
        verify(exactly = 1) { outbox.enqueue(any(), any()) }
    }
})
