package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailMessage
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SolicitacaoDeliberadaOutboxHandler(
    private val solicitacaoRepository: SolicitacaoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val mailPort: MailPort,
    private val objectMapper: ObjectMapper,
) : OutboxEventoHandler {
    override val tipos: Set<String> = setOf("solicitacao.deliberada", "solicitacao.ajuste_solicitado")

    override fun handle(evento: OutboxClaim) {
        val json = objectMapper.readTree(evento.payload)
        val solicitacaoIdRaw = json.path("solicitacaoId").asText(null)
        val protocolo = json.path("protocolo").asText("—")
        val estado = json.path("estado").asText("atualizado")
        if (solicitacaoIdRaw.isNullOrBlank()) {
            LOG.info("{} sem solicitacaoId; despacho sem SMTP.", evento.tipo)
            return
        }
        val solicitacao = solicitacaoRepository.findById(UUID.fromString(solicitacaoIdRaw)).orElse(null)
        if (solicitacao == null) {
            LOG.info("{} solicitação ausente; despacho sem SMTP.", evento.tipo)
            return
        }
        val aluno = usuarioRepository.findById(solicitacao.solicitanteId).orElse(null)
        if (aluno == null || !aluno.ativo) {
            LOG.info("{} sem destinatário ativo; despacho sem SMTP.", evento.tipo)
            return
        }
        val assunto = if (evento.tipo == "solicitacao.ajuste_solicitado") {
            "Solicitação $protocolo: ajustes solicitados"
        } else {
            "Solicitação $protocolo: $estado"
        }
        mailPort.send(
            MailMessage(
                aluno.emailInstitucional.value,
                assunto,
                """
                Sua solicitação $protocolo foi atualizada para o estado $estado.

                Acompanhe o andamento em Minhas solicitações. Este e-mail não contém deep-link de deliberação.
                """.trimIndent(),
            ),
        )
        LOG.info(
            "{} notificado ao solicitante {}",
            evento.tipo,
            EmailMascarado.de(aluno.emailInstitucional.value),
        )
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SolicitacaoDeliberadaOutboxHandler::class.java)
    }
}
