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
 * E-mail de boas-vindas do cadastro de aluno (F5.6-D02).
 * A senha temporária não é logada. O reset da secretaria usa outro handler.
 */
@Component
class BoasVindasAlunoOutboxHandler(
    private val usuarioRepository: UsuarioRepository,
    private val mailPort: MailPort,
    private val objectMapper: ObjectMapper,
) : OutboxEventoHandler {
    override val tipos: Set<String> = setOf("aluno.boas-vindas")

    override fun handle(evento: OutboxClaim) {
        val json = objectMapper.readTree(evento.payload)
        val usuarioIdRaw = json.path("usuarioId").asText(null)
        val senha = json.path("senhaTemporaria").asText(null)
        if (usuarioIdRaw.isNullOrBlank() || senha.isNullOrBlank()) {
            LOG.info("aluno.boas-vindas sem destinatário ou sem senha; despacho sem SMTP.")
            return
        }
        val usuario = usuarioRepository.findById(UUID.fromString(usuarioIdRaw)).orElse(null)
        if (usuario == null || !usuario.ativo) {
            LOG.info("aluno.boas-vindas sem destinatário ativo; despacho sem SMTP.")
            return
        }
        val nome = json.path("nome").asText(usuario.nome)
        val (assunto, corpo) = MailComTemplate(
            "Bem-vindo à Secretaria Online 2",
            """
            Olá, $nome.

            Sua conta de aluno foi criada. O login é o GRR ou o e-mail institucional.
            Senha temporária: $senha

            No primeiro acesso você define uma senha forte e registra o aceite da LGPD.
            A secretaria não tem acesso a esta senha.
            """.trimIndent(),
        )
        mailPort.send(MailMessage(usuario.emailInstitucional.value, assunto, corpo))
        LOG.info("aluno.boas-vindas despachado para {}", EmailMascarado.de(usuario.emailInstitucional.value))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BoasVindasAlunoOutboxHandler::class.java)
    }
}
