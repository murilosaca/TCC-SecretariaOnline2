package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailMessage
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Template distinto do esqueci-minha-senha (PASSWORD_RESET).
 * O corpo leva o link de uso único, nunca uma senha em claro (F5.6-D04).
 */
@Component
class PasswordResetAdminOutboxHandler(
    private val usuarioRepository: UsuarioRepository,
    private val mailPort: MailPort,
    private val objectMapper: ObjectMapper,
) : OutboxEventoHandler {
    override val tipos: Set<String> = setOf("PASSWORD_RESET_BY_ADMIN")

    override fun handle(evento: OutboxClaim) {
        val json = objectMapper.readTree(evento.payload)
        val usuarioIdRaw = json.path("usuarioId").asText(null)
        val resetUrl = json.path("resetUrl").asText(null)
        if (usuarioIdRaw.isNullOrBlank() || resetUrl.isNullOrBlank()) {
            LOG.info("PASSWORD_RESET_BY_ADMIN sem usuarioId/resetUrl; despacho sem SMTP.")
            return
        }
        val usuario = usuarioRepository.findById(UUID.fromString(usuarioIdRaw)).orElse(null)
        if (usuario == null || !usuario.ativo) {
            LOG.info("PASSWORD_RESET_BY_ADMIN sem destinatário ativo; despacho sem SMTP.")
            return
        }
        val (assunto, corpo) = MailComTemplate(
            "Redefinição de senha pela secretaria — Secretaria Online 2",
            """
            A secretaria acadêmica pediu a redefinição da senha desta conta.

            Use o link abaixo para escolher uma nova senha (válido por 24 horas):
            $resetUrl

            Este aviso não contém senha. Se você não esperava esta mensagem, procure a secretaria.
            """.trimIndent(),
        )
        mailPort.send(MailMessage(usuario.emailInstitucional.value, assunto, corpo))
        LOG.info("PASSWORD_RESET_BY_ADMIN despachado para {}", EmailMascarado.de(usuario.emailInstitucional.value))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PasswordResetAdminOutboxHandler::class.java)
    }
}
