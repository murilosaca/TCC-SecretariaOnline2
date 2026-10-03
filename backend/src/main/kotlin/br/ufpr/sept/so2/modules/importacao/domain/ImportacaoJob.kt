package br.ufpr.sept.so2.modules.importacao.domain

import java.time.OffsetDateTime
import java.util.UUID

data class ImportacaoLinha(
    val id: UUID,
    val numero: Int,
    val status: String,
    val mensagem: String?,
    val valores: Map<String, String>,
)

data class ImportacaoJob(
    val id: UUID,
    val kind: String,
    val status: String,
    val operadorId: UUID,
    val nomeArquivo: String,
    val checksumSha256: String,
    val totalLinhas: Int,
    val validCount: Int,
    val errorCount: Int,
    val warningCount: Int,
    val importadas: Int,
    val mensagem: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val linhas: List<ImportacaoLinha> = emptyList(),
)

data class ExportacaoJob(
    val id: UUID,
    val kind: String,
    val status: String,
    val operadorId: UUID,
    val filtrosJson: String?,
    val storageKey: String?,
    val nomeArquivo: String?,
    val expiresAt: OffsetDateTime?,
    val mensagem: String?,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    fun expirada(agora: OffsetDateTime): Boolean =
        status == ExportacaoStatus.PRONTO && expiresAt != null && !expiresAt.isAfter(agora)
}

object ImportacaoStatus {
    const val RECEBIDO = "RECEBIDO"
    const val VALIDANDO = "VALIDANDO"
    const val VALIDATED = "VALIDATED"
    const val FAILED = "FAILED"
    const val SUCCESS = "SUCCESS"
    const val PARTIAL = "PARTIAL"
    const val PENDING = "PENDING"
    const val VALID = "VALID"
    const val INVALID = "INVALID"
    const val WARNING = "WARNING"
}

object ExportacaoStatus {
    const val PROCESSANDO = "PROCESSANDO"
    const val PRONTO = "PRONTO"
    const val EXPIRADO = "EXPIRADO"
    const val FAILED = "FAILED"
}

data class ResultadoLotes(
    val status: String,
    val importadas: Int,
    val naoProcessadas: Int,
    val mensagem: String?,
)

fun executarLotes(
    lotes: List<List<ImportacaoLinha>>,
    gravar: (List<ImportacaoLinha>) -> Unit,
): ResultadoLotes {
    var importadas = 0
    val total = lotes.sumOf { it.size }
    for (lote in lotes) {
        try {
            gravar(lote)
            importadas += lote.size
        } catch (ex: Exception) {
            return ResultadoLotes(
                ImportacaoStatus.PARTIAL,
                importadas,
                total - importadas,
                ex.message ?: "Falha no lote seguinte.",
            )
        }
    }
    return ResultadoLotes(ImportacaoStatus.SUCCESS, importadas, 0, null)
}
