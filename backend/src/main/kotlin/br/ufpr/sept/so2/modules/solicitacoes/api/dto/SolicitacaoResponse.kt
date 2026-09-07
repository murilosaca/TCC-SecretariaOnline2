package br.ufpr.sept.so2.modules.solicitacoes.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class SolicitacaoResponse(
    val id: UUID,
    val protocolo: String,
    val tipoCodigo: String,
    val tipoNome: String,
    val tipoVersao: Int,
    val estado: String,
    val payload: Map<String, Any?>,
    val formSchema: Map<String, Any?>?,
    val prazoEm: OffsetDateTime?,
    val prazoVencido: Boolean,
    val sla: String,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
    val eventos: List<SolicitacaoEventoResponse>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)
