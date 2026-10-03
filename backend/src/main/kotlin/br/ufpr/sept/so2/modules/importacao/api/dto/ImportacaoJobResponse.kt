package br.ufpr.sept.so2.modules.importacao.api.dto

import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoJob
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoLinha
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoStatus
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class ImportacaoLinhaResponse(
    val numero: Int,
    val status: String,
    val mensagem: String?,
    val valores: Map<String, String>,
)

data class ImportacaoJobResponse(
    val id: UUID,
    val kind: String,
    val status: String,
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
    val linhas: List<ImportacaoLinhaResponse>?,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun de(job: ImportacaoJob, comLinhas: Boolean): ImportacaoJobResponse {
            val links = linkedMapOf("self" to "/importacoes/${job.id}")
            if (job.status == ImportacaoStatus.VALIDATED && job.errorCount == 0) {
                links["confirm"] = "/importacoes/${job.id}/confirmar"
            }
            return ImportacaoJobResponse(
                id = job.id,
                kind = job.kind,
                status = job.status,
                nomeArquivo = job.nomeArquivo,
                checksumSha256 = job.checksumSha256,
                totalLinhas = job.totalLinhas,
                validCount = job.validCount,
                errorCount = job.errorCount,
                warningCount = job.warningCount,
                importadas = job.importadas,
                mensagem = job.mensagem,
                createdAt = job.createdAt,
                updatedAt = job.updatedAt,
                linhas = if (comLinhas) job.linhas.map(::linha) else null,
                links = links,
            )
        }

        private fun linha(linha: ImportacaoLinha) = ImportacaoLinhaResponse(
            linha.numero,
            linha.status,
            linha.mensagem,
            linha.valores,
        )
    }
}
