package br.ufpr.sept.so2.modules.bff.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ProfessorDashboardResponse(
    val saudacao: SaudacaoResponse,
    val kpis: KpisResponse,
    val filaSolicitacoes: List<FilaSolicitacaoResponse>?,
    val meusEventos: List<MeuEventoResponse>?,
    val formativasCaaf: List<ItemFilaResponse>?,
    val estagiosPendentes: List<ItemFilaResponse>?,
    val tccsPendentes: List<ItemFilaResponse>?,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class SaudacaoResponse(val nome: String)

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class KpisResponse(
        val pendentesDeliberar: Int?,
        val formativasRevisao: Int?,
        val eventosHoje: Int?,
        val slaUrgentes: Int?,
    )

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class FilaSolicitacaoResponse(
        val id: UUID,
        val protocolo: String,
        val tipoNome: String,
        val estado: String,
        val prazoEm: OffsetDateTime?,
        val slaVencido: Boolean,
        val urgente: Boolean,
        val href: String,
    )

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class MeuEventoResponse(
        val id: UUID,
        val titulo: String,
        val inicioEm: OffsetDateTime,
        val fimEm: OffsetDateTime,
        val estado: String,
        @get:JsonProperty("_links")
        val links: Map<String, String>,
    )

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class ItemFilaResponse(
        val id: UUID,
        val titulo: String,
        val estado: String,
        val href: String,
    )
}
