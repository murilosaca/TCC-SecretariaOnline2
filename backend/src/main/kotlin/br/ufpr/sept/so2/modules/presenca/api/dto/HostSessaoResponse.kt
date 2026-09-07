package br.ufpr.sept.so2.modules.presenca.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class HostSessaoResponse(
    val eventoId: UUID,
    val titulo: String,
    val attendanceMode: String,
    val estado: String,
    val janelaAtiva: Boolean,
    val janelaExpira: OffsetDateTime?,
    val pin: String?,
    val presentes: Long,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)
