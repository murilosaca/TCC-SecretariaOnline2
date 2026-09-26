package br.ufpr.sept.so2.modules.reports.domain

import java.util.UUID

/**
 * Agregado de leitura do relatório F6.2 (RF-F6-002).
 * Domínio puro: sem Spring/JPA.
 */
data class RelatorioCoordenador(
    val cursoId: UUID,
    val cursoSigla: String,
    val cursoNome: String,
    val periodoCodigo: String?,
    val periodoRotulo: String?,
    val kpis: KpisCoordenador,
    val series: SeriesCoordenador,
    val pendencias: List<PendenciaCoordenador>,
    val cargaPorDeliberador: List<CargaDeliberador>,
) {
    data class KpisCoordenador(
        val tempoMedioDias: Double,
        val taxaIndeferimento: Double,
        val horasValidadas: Int,
        val taxaPresenca: Double,
        val thresholdIndeferimento: Double,
    )

    data class SeriesCoordenador(
        val evasao: List<PontoEvasao>,
        val formativas: List<PontoFormativa>,
        val aprovacaoFormativas: List<PontoAprovacao>,
    )

    data class PontoEvasao(
        val periodo: String,
        val ativos: Int,
        val evadidos: Int,
    )

    data class PontoFormativa(
        val periodo: String,
        val horasValidadas: Int,
        val aprovadas: Int,
        val indeferidas: Int,
    )

    data class PontoAprovacao(
        val periodo: String,
        val taxaAprovacao: Double,
    )

    data class PendenciaCoordenador(
        val id: UUID,
        val descricao: String,
        val href: String,
    )

    data class CargaDeliberador(
        val nome: String,
        val quantidade: Int,
        val tempoMedioDias: Double,
    )

    companion object {
        /** Limiar de alerta da CA-F6-002-03 (20%). Sem coluna dedicada nesta fatia. */
        const val THRESHOLD_INDEFERIMENTO_PADRAO: Double = 0.20
    }
}
