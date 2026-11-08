package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoEntregaRepository
import br.ufpr.sept.so2.modules.comunicacao.application.ports.EmailUsuarioPort
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailMessage
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ComunicacaoDigestOutboxHandler(
    private val emailUsuarioPort: EmailUsuarioPort,
    private val entregaRepository: ComunicacaoEntregaRepository,
    private val mailPort: MailPort,
    private val objectMapper: ObjectMapper,
) : OutboxEventoHandler {
    override val tipos: Set<String> = setOf(EnfileirarDigestUseCase.TIPO)

    override fun handle(evento: OutboxClaim) {
        val json = objectMapper.readTree(evento.payload)
        val usuarioId = UUID.fromString(json.path("usuarioId").asText())
        val titulos = json.path("titulos").map { it.asText() }
        val entregaIds = json.path("entregaIds").map { UUID.fromString(it.asText()) }
        val pendentes = entregaIds.mapNotNull { entregaRepository.findById(it) }.filter { !it.emailEnviado }
        if (pendentes.isEmpty() && entregaIds.isNotEmpty()) {
            return
        }
        val email = emailUsuarioPort.emailDe(usuarioId)
            ?: throw IllegalStateException("Destinatário do digest não encontrado.")
        val corpo = if (titulos.isEmpty()) "Não há comunicados novos." else titulos.joinToString("\n") { "• $it" }
        val (assunto, texto) = MailComTemplate("Resumo de comunicados", corpo)
        mailPort.send(MailMessage(email, assunto, texto))
        pendentes.forEach { entrega ->
            entrega.emailEnviado = true
            entregaRepository.save(entrega)
        }
    }
}
