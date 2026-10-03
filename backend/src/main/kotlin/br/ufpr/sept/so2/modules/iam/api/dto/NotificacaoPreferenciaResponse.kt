package br.ufpr.sept.so2.modules.iam.api.dto

import br.ufpr.sept.so2.modules.iam.domain.CanalNotificacao
import br.ufpr.sept.so2.modules.iam.domain.NotificacaoPreferencia
import br.ufpr.sept.so2.modules.iam.domain.PrioridadeNotificacao
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.format.DateTimeFormatter

data class CanalPreferenciaResponse(
    val email: Boolean,
    val inApp: Boolean,
)

data class NotificacaoPreferenciaResponse(
    val canais: Map<String, CanalPreferenciaResponse>,
    val dndInicio: String?,
    val dndFim: String?,
    val digest: String,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        private val HORA = DateTimeFormatter.ofPattern("HH:mm")

        fun from(preferencia: NotificacaoPreferencia): NotificacaoPreferenciaResponse =
            NotificacaoPreferenciaResponse(
                PrioridadeNotificacao.entries.associate { prioridade ->
                    val canais = preferencia.canais[prioridade].orEmpty()
                    prioridade.name to CanalPreferenciaResponse(
                        canais[CanalNotificacao.EMAIL] ?: true,
                        canais[CanalNotificacao.IN_APP] ?: true,
                    )
                },
                preferencia.dndInicio?.format(HORA),
                preferencia.dndFim?.format(HORA),
                preferencia.digest.name,
                mapOf(
                    "self" to "/me/notifications",
                    "update" to "/me/notifications",
                ),
            )
    }
}
