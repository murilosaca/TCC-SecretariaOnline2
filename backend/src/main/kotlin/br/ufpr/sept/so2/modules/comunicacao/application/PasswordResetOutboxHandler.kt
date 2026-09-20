package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailMessage
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PasswordResetOutboxHandler(
    private val usuarioRepository: UsuarioRepository,
    private val mailPort: MailPort,
    private val objectMapper: ObjectMapper,
) : OutboxEventoHandler {
    override val tipos: Set<String> = setOf("PASSWORD_RESET")

    override fun handle(evento: OutboxClaim) {
        val json = objectMapper.readTree(evento.payload)
        val usuarioIdRaw = json.path("usuarioId").asText(null)
        val resetUrl = json.path("resetUrl").asText(null)
        if (usuarioIdRaw.isNullOrBlank() || resetUrl.isNullOrBlank()) {
            LOG.info("PASSWORD_RESET sem usuarioId/resetUrl; despacho sem SMTP.")
            return
        }
        val usuario = usuarioRepository.findById(UUID.fromString(usuarioIdRaw)).orElse(null)
        if (usuario == null || !usuario.ativo) {
            LOG.info("PASSWORD_RESET sem destinatário ativo; despacho sem SMTP.")
            return
        }
        mailPort.send(
            MailMessage(
                usuario.emailInstitucional.value,
                "Redefinição de senha — Secretaria Online 2",
                """
                Recebemos um pedido para redefinir a senha desta conta.

                Se foi você, use o link abaixo (válido por 24 horas):
                $resetUrl

                Se você não pediu esta redefinição, ignore esta mensagem.
                """.trimIndent(),
            ),
        )
        LOG.info("PASSWORD_RESET despachado para {}", EmailMascarado.de(usuario.emailInstitucional.value))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PasswordResetOutboxHandler::class.java)
    }
}
