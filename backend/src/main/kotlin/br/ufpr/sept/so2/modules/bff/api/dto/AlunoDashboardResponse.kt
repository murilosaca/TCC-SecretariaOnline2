package br.ufpr.sept.so2.modules.bff.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.ALWAYS)
data class AlunoDashboardResponse(
    val saudacao: SaudacaoResponse,
    val periodoVigente: PeriodoVigenteResponse?,
    val alertaPeriodoAusente: Boolean?,
    val kpis: KpisResponse,
    val pendencias: List<PendenciaResponse>?,
    val ultimasSolicitacoes: List<UltimaSolicitacaoResponse>?,
    val proximosEventos: List<ProximoEventoResponse>?,
    val pendenciasFormativas: List<PendenciaResponse>?,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class SaudacaoResponse(val nome: String, val curso: String?)

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
        val horasFormativas: KpiHorasFormativas?,
        val solicitacoesAbertas: Int?,
        val eventosHoje: Int?,
        val certificados: Int?,
    )

    data class KpiHorasFormativas(val validadas: Int, val requeridas: Int)

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class PendenciaResponse(
        val id: UUID,
        val titulo: String,
        val estado: String,
        val href: String,
    )

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class UltimaSolicitacaoResponse(
        val id: UUID,
        val protocolo: String,
        val tipoNome: String,
        val estado: String,
        val prazoEm: OffsetDateTime?,
        val slaVencido: Boolean,
    )

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class ProximoEventoResponse(
        val id: UUID,
        val titulo: String,
        val inicioEm: OffsetDateTime,
        val fimEm: OffsetDateTime,
        val janelaAtiva: Boolean,
        val href: String,
    )
}
