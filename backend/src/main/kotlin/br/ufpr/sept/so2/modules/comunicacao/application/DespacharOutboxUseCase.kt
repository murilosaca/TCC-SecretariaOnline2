package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.infrastructure.ComunicacaoProperties
import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class DespacharOutboxUseCase(
    private val outboxPort: OutboxPort,
    private val handlers: List<OutboxEventoHandler>,
    private val auditLogPort: AuditLogPort,
    private val properties: ComunicacaoProperties,
) {
    fun execute() {
        val staleBefore = OffsetDateTime.now().minusSeconds(properties.staleProcessingSeconds)
        val lote = outboxPort.claimPending(properties.lote, staleBefore)
        lote.forEach { despacharUm(it) }
    }

    private fun despacharUm(evento: OutboxClaim) {
        try {
            handlerPara(evento.tipo).handle(evento)
            outboxPort.markSent(evento.id)
        } catch (ex: Exception) {
            registrarFalha(evento, ex)
        }
    }

    private fun handlerPara(tipo: String): OutboxEventoHandler =
        handlers.firstOrNull { tipo in it.tipos } ?: FALLBACK_NOOP

    private fun registrarFalha(evento: OutboxClaim, ex: Exception) {
        val tentativas = evento.tentativas + 1
        val erro = EmailMascarado.sanitizarErro(ex.message)
        if (tentativas >= properties.maxTentativas) {
            outboxPort.markFailed(evento.id, tentativas, erro)
            auditLogPort.append(
                "outbox.failed",
                null,
                "{\"id\":\"${evento.id}\",\"tipo\":\"${evento.tipo}\"}",
                null,
            )
            LOG.warn("Outbox {} id={} FAILED após {} tentativas", evento.tipo, evento.id, tentativas)
            return
        }
        outboxPort.markPendingRetry(evento.id, tentativas, erro)
        LOG.warn("Outbox {} id={} volta a PENDING tentativa={}", evento.tipo, evento.id, tentativas)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DespacharOutboxUseCase::class.java)
        private val FALLBACK_NOOP = object : OutboxEventoHandler {
            override val tipos: Set<String> = emptySet()
            override fun handle(evento: OutboxClaim) = Unit
        }
    }
}
