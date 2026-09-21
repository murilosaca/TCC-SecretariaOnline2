package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Tipos enfileirados hoje que ainda não disparam e-mail nesta fatia.
 * Marcar SENT (no-op) evita retry eterno. Sem push.
 * estagio.documento_enviado, estagio.parecer_emitido e estagio.encerrado
 * seguem o mesmo no-op de certificado.emitido.
 */
@Component
class NoOpOutboxHandler : OutboxEventoHandler {
    override val tipos: Set<String> = setOf(
        "iam.first_access_completed",
        "formativa.criada",
        "formativa.confirmada",
        "formativa.aprovada",
        "formativa.indeferida",
        "certificado.emitido",
        "estagio.documento_enviado",
        "estagio.parecer_emitido",
        "estagio.encerrado",
        "presenca.confirmada",
        "evento.janela_aberta",
        "evento.qr_renovado",
        "evento.encerrado",
    )

    override fun handle(evento: OutboxClaim) {
        LOG.debug("Outbox {} id={} sem e-mail nesta fatia; SENT no-op.", evento.tipo, evento.id)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NoOpOutboxHandler::class.java)
    }
}
