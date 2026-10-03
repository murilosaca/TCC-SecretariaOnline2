package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.AudienciaPort
import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoEntregaRepository
import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoRepository
import br.ufpr.sept.so2.modules.comunicacao.application.ports.DestinatarioComunicacao
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailMessage
import br.ufpr.sept.so2.modules.comunicacao.application.ports.MailPort
import br.ufpr.sept.so2.modules.comunicacao.domain.CanalEntrega
import br.ufpr.sept.so2.modules.comunicacao.domain.CanaisEntrega
import br.ufpr.sept.so2.modules.comunicacao.domain.Comunicacao
import br.ufpr.sept.so2.modules.comunicacao.domain.ComunicacaoEntrega
import br.ufpr.sept.so2.modules.comunicacao.domain.PreferenciaEntrega
import br.ufpr.sept.so2.modules.comunicacao.domain.PrioridadeComunicacao
import br.ufpr.sept.so2.modules.iam.application.ports.NotificacaoPreferenciaRepository
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import br.ufpr.sept.so2.modules.iam.domain.CanalNotificacao
import br.ufpr.sept.so2.modules.iam.domain.ModoDigest
import br.ufpr.sept.so2.modules.iam.domain.NotificacaoPreferencia
import br.ufpr.sept.so2.modules.iam.domain.PrioridadeNotificacao
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

@Component
class ComunicacaoPublicadaOutboxHandler(
    private val comunicacaoRepository: ComunicacaoRepository,
    private val entregaRepository: ComunicacaoEntregaRepository,
    private val audienciaPort: AudienciaPort,
    private val preferenciaRepository: NotificacaoPreferenciaRepository,
    private val mailPort: MailPort,
    private val objectMapper: ObjectMapper,
) : OutboxEventoHandler {
    override val tipos: Set<String> = setOf(PublicarComunicacaoUseCase.TIPO_OUTBOX)

    @Transactional
    override fun handle(evento: OutboxClaim) {
        val comunicacaoId = comunicacaoId(evento.payload) ?: return
        val comunicacao = comunicacaoRepository.findById(comunicacaoId)
        if (comunicacao == null) {
            LOG.info("comunicacao.published sem registro id={}; despacho sem entrega.", comunicacaoId)
            return
        }
        val agora = LocalTime.now(CanaisEntrega.ZONA)
        val destinatarios = audienciaPort.destinatarios(comunicacao.audienciaTipo, comunicacao.audienciaId, comunicacao.autorId)
        destinatarios.forEach { entregar(comunicacao, it, agora) }
        LOG.info("comunicacao.published id={} destinatarios={}", comunicacao.id, destinatarios.size)
    }

    private fun entregar(comunicacao: Comunicacao, destinatario: DestinatarioComunicacao, agora: LocalTime) {
        val preferencia = preferenciaRepository.findByUsuarioId(destinatario.usuarioId).orElse(null)
        val canais = CanaisEntrega.resolver(
            comunicacao.prioridade,
            preferencia?.let { dePreferencia(it, comunicacao.prioridade) },
            agora,
        )
        if (canais.isEmpty()) {
            return
        }
        val entrega = garantir(comunicacao, destinatario.usuarioId, CanalEntrega.IN_APP in canais)
        if (CanalEntrega.EMAIL in canais && !entrega.emailEnviado) {
            mailPort.send(MailMessage(destinatario.email, comunicacao.titulo, comunicacao.corpo))
            entrega.emailEnviado = true
            entregaRepository.save(entrega)
            LOG.info("Comunicado enviado por e-mail a {}", EmailMascarado.de(destinatario.email))
        }
    }

    private fun garantir(comunicacao: Comunicacao, destinatarioId: UUID, inApp: Boolean): ComunicacaoEntrega {
        val existente = entregaRepository.findByComunicacaoEDestinatario(comunicacao.id, destinatarioId)
        if (existente != null) {
            if (inApp && !existente.inApp) {
                existente.inApp = true
                return entregaRepository.save(existente)
            }
            return existente
        }
        return entregaRepository.save(
            ComunicacaoEntrega(
                Uuids.v7(),
                comunicacao.id,
                destinatarioId,
                null,
                null,
                inApp,
                false,
                OffsetDateTime.now(),
            ),
        )
    }

    private fun dePreferencia(preferencia: NotificacaoPreferencia, prioridade: PrioridadeComunicacao): PreferenciaEntrega {
        val mapa = preferencia.canais[PrioridadeNotificacao.valueOf(prioridade.name)].orEmpty()
        return PreferenciaEntrega(
            emailLigado = mapa[CanalNotificacao.EMAIL] == true,
            inAppLigado = mapa[CanalNotificacao.IN_APP] == true,
            dndInicio = preferencia.dndInicio,
            dndFim = preferencia.dndFim,
            digestResumo = preferencia.digest == ModoDigest.RESUMO,
        )
    }

    private fun comunicacaoId(payload: String): UUID? {
        val bruto = objectMapper.readTree(payload).path("comunicacaoId").asText(null)
        if (bruto.isNullOrBlank()) {
            LOG.info("comunicacao.published sem comunicacaoId; despacho sem entrega.")
            return null
        }
        return UUID.fromString(bruto)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ComunicacaoPublicadaOutboxHandler::class.java)
    }
}
