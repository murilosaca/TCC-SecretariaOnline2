package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.EmailUsuarioPort
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailMessage
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * exportacao.pronta sai pelo template. Sem revisão publicada, o dispatcher
 * marca FAILED com last_error — não SENT silencioso (RF-F5-010).
 */
@Component
class ExportacaoProntaOutboxHandler(
    private val emailUsuarioPort: EmailUsuarioPort,
    private val mailPort: MailPort,
    private val objectMapper: ObjectMapper,
) : OutboxEventoHandler {
    override val tipos: Set<String> = setOf(TIPO)

    override fun handle(evento: OutboxClaim) {
        val mensagem = TemplateMensagemAtual.atual()
            ?: throw TemplateAusenteException("Template exportacao.pronta ausente.")
        val operadorId = operadorId(evento.payload)
            ?: throw IllegalStateException("Exportação sem operador.")
        val email = emailUsuarioPort.emailDe(operadorId)
            ?: throw IllegalStateException("Operador da exportação não encontrado.")
        mailPort.send(MailMessage(email, mensagem.assunto, mensagem.corpo))
    }

    private fun operadorId(payload: String): UUID? {
        val bruto = objectMapper.readTree(payload).path("operadorId").asText(null)
        if (bruto.isNullOrBlank()) {
            return null
        }
        return UUID.fromString(bruto)
    }

    companion object {
        const val TIPO: String = "exportacao.pronta"
    }
}
