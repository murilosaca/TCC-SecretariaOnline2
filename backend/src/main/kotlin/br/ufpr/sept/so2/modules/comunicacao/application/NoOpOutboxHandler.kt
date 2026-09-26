package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Tipos enfileirados hoje que ainda não disparam e-mail nesta fatia.
 * Marcar SENT (no-op) evita retry eterno. Sem push.
 * estagio.documento_enviado, estagio.parecer_emitido, estagio.encerrado,
 * estagio.orientador_atribuido, estagio.registrado e estagio.atualizado
 * seguem o mesmo no-op de certificado.emitido.
 * tcc.submitted e tcc.reviewed também fecham SENT sem SMTP.
 */
@Component
class NoOpOutboxHandler : OutboxEventoHandler {
    override val tipos: Set<String> = setOf(
        "iam.first_access_completed",
        "iam.user_created",
        "formativa.criada",
        "formativa.confirmada",
        "formativa.aprovada",
        "formativa.indeferida",
        "certificado.emitido",
        "estagio.documento_enviado",
        "estagio.parecer_emitido",
        "estagio.encerrado",
        "estagio.orientador_atribuido",
        "estagio.registrado",
        "estagio.atualizado",
        "tcc.submitted",
        "tcc.reviewed",
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
