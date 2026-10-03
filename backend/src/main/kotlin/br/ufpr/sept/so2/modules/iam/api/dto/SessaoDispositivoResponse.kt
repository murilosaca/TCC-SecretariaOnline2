package br.ufpr.sept.so2.modules.iam.api.dto

import br.ufpr.sept.so2.modules.iam.application.SessaoDispositivoVisao
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class SessoesResponse(
    val itens: List<SessaoDispositivoResponse>,
)

data class SessaoDispositivoResponse(
    val id: UUID,
    val dispositivo: String,
    val atual: Boolean,
    val criadaEm: OffsetDateTime,
    val expiraEm: OffsetDateTime,
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(visao: SessaoDispositivoVisao): SessaoDispositivoResponse {
            val links = linkedMapOf<String, String>()
            if (visao.encerrar) {
                links["encerrar"] = "/me/sessions/${visao.id}"
            }
            return SessaoDispositivoResponse(
                visao.id,
                visao.dispositivo,
                visao.atual,
                visao.criadaEm,
                visao.expiraEm,
                links,
            )
        }
    }
}
