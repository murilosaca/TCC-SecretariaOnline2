package br.ufpr.sept.so2.modules.reports.domain

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Agregado de leitura do relatório F5.18 (RF-F5-011).
 * Domínio puro: sem Spring/JPA.
 */
data class RelatorioSecretaria(
    val cursoId: UUID?,
    val cursoSigla: String?,
    val cursoNome: String,
    val cursosEscopo: List<CursoEscopoResumo>,
    val periodoCodigo: String?,
    val periodoRotulo: String?,
    val solicitacoesPorTipo: List<PontoTipo>,
    val solicitacoesPorEstado: List<PontoEstado>,
    val presencas: List<PontoPresenca>,
    val horasFormativas: List<PontoHoras>,
    val itensSolicitacao: List<ItemSolicitacao>,
) {
    data class CursoEscopoResumo(
        val id: UUID,
        val sigla: String,
        val nome: String,
    )

    data class PontoTipo(
        val tipoCodigo: String,
        val tipoNome: String,
        val quantidade: Int,
    )

    data class PontoEstado(
        val estado: String,
        val quantidade: Int,
    )

    data class PontoPresenca(
        val periodo: String,
        val confirmadas: Int,
        val registradas: Int,
    )

    data class PontoHoras(
        val periodo: String,
        val horasValidadas: Int,
    )

    data class ItemSolicitacao(
        val id: UUID,
        val protocolo: String,
        val tipoCodigo: String,
        val tipoNome: String,
        val estado: String,
        val createdAt: OffsetDateTime,
        val cursoSigla: String,
    )
}
