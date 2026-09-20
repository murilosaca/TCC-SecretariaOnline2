package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailMessage
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailPort
import br.ufpr.sept.so2.modules.iam.application.IamSettings
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SolicitacaoCriadaOutboxHandler(
    private val usuarioRepository: UsuarioRepository,
    private val jwtTokenService: JwtTokenService,
    private val mailPort: MailPort,
    private val iamSettings: IamSettings,
    private val objectMapper: ObjectMapper,
) : OutboxEventoHandler {
    override val tipos: Set<String> = setOf("solicitacao.criada")

    override fun handle(evento: OutboxClaim) {
        val json = objectMapper.readTree(evento.payload)
        val solicitacaoIdRaw = json.path("solicitacaoId").asText(null)
        val protocolo = json.path("protocolo").asText("—")
        if (solicitacaoIdRaw.isNullOrBlank()) {
            LOG.info("solicitacao.criada sem solicitacaoId; despacho sem SMTP.")
            return
        }
        val solicitacaoId = UUID.fromString(solicitacaoIdRaw)
        val destinatarios = usuarioRepository.findAtivosByAuthority(AUTHORITY_DELIBERATE)
            .filter { AUTHORITY_TRIAGE !in it.authorities }
        if (destinatarios.isEmpty()) {
            LOG.info("solicitacao.criada sem deliberante de deep-link; despacho sem SMTP.")
            return
        }
        val base = iamSettings.frontendBaseUrl.trimEnd('/')
        destinatarios.forEach { professor ->
            enviar(professor, solicitacaoId, protocolo, base)
        }
    }

    private fun enviar(professor: Usuario, solicitacaoId: UUID, protocolo: String, base: String) {
        val jwt = jwtTokenService.emitDeliberationToken(professor, solicitacaoId)
        val url = "$base/solicitacoes/$solicitacaoId/deliberar?token=$jwt"
        mailPort.send(
            MailMessage(
                professor.emailInstitucional.value,
                "Solicitação $protocolo aguarda deliberação",
                """
                Há uma solicitação aguardando sua deliberação ($protocolo).

                Acesse o link (válido por 72 horas, uso único após decidir):
                $url

                A secretaria delibera pela fila, sem este deep-link.
                """.trimIndent(),
            ),
        )
        LOG.info(
            "Deep-link de deliberação enviado a {} protocolo={}",
            EmailMascarado.de(professor.emailInstitucional.value),
            protocolo,
        )
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SolicitacaoCriadaOutboxHandler::class.java)
        const val AUTHORITY_DELIBERATE = "request.deliberate"
        const val AUTHORITY_TRIAGE = "request.triage"
    }
}
