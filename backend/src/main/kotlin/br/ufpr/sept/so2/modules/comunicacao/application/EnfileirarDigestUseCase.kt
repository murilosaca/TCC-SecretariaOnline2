package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoEntregaRepository
import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoRepository
import br.ufpr.sept.so2.modules.comunicacao.domain.CanaisEntrega
import br.ufpr.sept.so2.modules.comunicacao.domain.PreferenciaEntrega
import br.ufpr.sept.so2.modules.iam.application.ports.NotificacaoPreferenciaRepository
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.domain.ModoDigest
import br.ufpr.sept.so2.modules.iam.domain.NotificacaoPreferencia
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime
import java.util.UUID

/**
 * Junta e-mails segurados por RESUMO ou DND e publica um único evento no Outbox.
 * Não envia SMTP aqui e não usa FCM (RF-F1-003-c / RF-TR-007).
 */
@Service
class EnfileirarDigestUseCase(
    private val entregaRepository: ComunicacaoEntregaRepository,
    private val comunicacaoRepository: ComunicacaoRepository,
    private val preferenciaRepository: NotificacaoPreferenciaRepository,
    private val outboxPort: OutboxPort,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(agora: LocalTime = LocalTime.now(CanaisEntrega.ZONA)): Int {
        val grupos = entregaRepository.findEmailAdiado().groupBy { it.destinatarioId }
        var enfileirados = 0
        grupos.forEach { (usuarioId, itens) ->
            val preferencia = preferenciaRepository.findByUsuarioId(usuarioId).orElse(null)
            if (preferencia != null && CanaisEntrega.emDnd(preferenciaDe(preferencia), agora)) {
                return@forEach
            }
            val titulosPorId = comunicacaoRepository.findByIds(itens.map { it.comunicacaoId })
                .associate { it.id to it.titulo }
            val payload = objectMapper.writeValueAsString(
                mapOf(
                    "usuarioId" to usuarioId.toString(),
                    "titulos" to itens.map { titulosPorId[it.comunicacaoId] ?: "Comunicado" },
                    "entregaIds" to itens.map { it.id.toString() },
                ),
            )
            outboxPort.enqueue(TIPO, payload)
            itens.forEach { entrega ->
                entrega.emailAdiado = false
                entregaRepository.save(entrega)
            }
            enfileirados += 1
        }
        return enfileirados
    }

    private fun preferenciaDe(preferencia: NotificacaoPreferencia): PreferenciaEntrega =
        PreferenciaEntrega(
            emailLigado = true,
            inAppLigado = true,
            dndInicio = preferencia.dndInicio,
            dndFim = preferencia.dndFim,
            digestResumo = preferencia.digest == ModoDigest.RESUMO,
        )

    companion object {
        const val TIPO: String = "comunicacao.digest"
    }
}
