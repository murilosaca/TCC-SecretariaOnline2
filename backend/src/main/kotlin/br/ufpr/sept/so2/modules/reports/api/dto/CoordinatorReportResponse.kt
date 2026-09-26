package br.ufpr.sept.so2.modules.reports.api.dto

import br.ufpr.sept.so2.modules.reports.domain.RelatorioCoordenador
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

@JsonInclude(JsonInclude.Include.ALWAYS)
data class CoordinatorReportResponse(
    val filtros: FiltrosResponse,
    val kpis: KpisResponse,
    val series: SeriesResponse,
    val pendencias: List<PendenciaResponse>,
    val cargaPorDeliberador: List<CargaDeliberadorResponse>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    data class FiltrosResponse(
        val periodo: String?,
        val periodoRotulo: String?,
        val curso: String,
        val cursoId: UUID,
        val cursoNome: String,
    )

    data class KpisResponse(
        val tempoMedioDias: Double,
        val taxaIndeferimento: Double,
        val horasValidadas: Int,
        val taxaPresenca: Double,
        val thresholdIndeferimento: Double,
    )

    data class SeriesResponse(
        val evasao: List<PontoEvasaoResponse>,
        val formativas: List<PontoFormativaResponse>,
        val aprovacaoFormativas: List<PontoAprovacaoResponse>,
    )

    data class PontoEvasaoResponse(
        val periodo: String,
        val ativos: Int,
        val evadidos: Int,
    )

    data class PontoFormativaResponse(
        val periodo: String,
        val horasValidadas: Int,
        val aprovadas: Int,
        val indeferidas: Int,
    )

    data class PontoAprovacaoResponse(
        val periodo: String,
        val taxaAprovacao: Double,
    )

    data class PendenciaResponse(
        val id: UUID,
        val descricao: String,
        val href: String,
    )

    data class CargaDeliberadorResponse(
        val nome: String,
        val quantidade: Int,
        val tempoMedioDias: Double,
    )

    companion object {
        fun from(relatorio: RelatorioCoordenador, links: Map<String, String>): CoordinatorReportResponse =
            CoordinatorReportResponse(
                filtros = FiltrosResponse(
                    periodo = relatorio.periodoCodigo,
                    periodoRotulo = relatorio.periodoRotulo,
                    curso = relatorio.cursoSigla,
                    cursoId = relatorio.cursoId,
                    cursoNome = relatorio.cursoNome,
                ),
                kpis = KpisResponse(
                    tempoMedioDias = relatorio.kpis.tempoMedioDias,
                    taxaIndeferimento = relatorio.kpis.taxaIndeferimento,
                    horasValidadas = relatorio.kpis.horasValidadas,
                    taxaPresenca = relatorio.kpis.taxaPresenca,
                    thresholdIndeferimento = relatorio.kpis.thresholdIndeferimento,
                ),
                series = SeriesResponse(
                    evasao = relatorio.series.evasao.map {
                        PontoEvasaoResponse(it.periodo, it.ativos, it.evadidos)
                    },
                    formativas = relatorio.series.formativas.map {
                        PontoFormativaResponse(it.periodo, it.horasValidadas, it.aprovadas, it.indeferidas)
                    },
                    aprovacaoFormativas = relatorio.series.aprovacaoFormativas.map {
                        PontoAprovacaoResponse(it.periodo, it.taxaAprovacao)
                    },
                ),
                pendencias = relatorio.pendencias.map {
                    PendenciaResponse(it.id, it.descricao, it.href)
                },
                cargaPorDeliberador = relatorio.cargaPorDeliberador.map {
                    CargaDeliberadorResponse(it.nome, it.quantidade, it.tempoMedioDias)
                },
                links = links,
            )
    }
}
