package br.ufpr.sept.so2.modules.bff.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.ALWAYS)
data class SecretaryDashboardResponse(
    val saudacao: SaudacaoResponse,
    val periodoVigente: PeriodoVigenteResponse?,
    val alertaPeriodoAusente: Boolean?,
    val kpis: KpisResponse,
    val alertasSla: Int?,
    val filaPriorizada: List<FilaItemResponse>?,
    val agendaDia: List<AgendaItemResponse>?,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class SaudacaoResponse(val nome: String)

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class PeriodoVigenteResponse(
        val id: UUID,
        val ano: Int,
        val semestre: Int,
        val rotulo: String,
        val inicio: LocalDate,
        val fim: LocalDate,
    )

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class KpisResponse(
        val abertas: Int?,
        val atrasadas: Int?,
        val concluidasHoje: Int?,
        val eventosDia: Int?,
    )

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class FilaItemResponse(
        val id: UUID,
        val protocolo: String,
        val tipoNome: String,
        val estado: String,
        val prazoEm: OffsetDateTime?,
        val slaStatus: String?,
        val href: String,
    )

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class AgendaItemResponse(
        val id: UUID,
        val titulo: String,
        val inicioEm: OffsetDateTime,
        val fimEm: OffsetDateTime,
        val estado: String,
    )
}
