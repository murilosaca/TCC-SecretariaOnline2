package br.ufpr.sept.so2.modules.importacao.api.dto

import br.ufpr.sept.so2.modules.importacao.domain.ExportacaoJob
import br.ufpr.sept.so2.modules.importacao.domain.ExportacaoKinds
import br.ufpr.sept.so2.modules.importacao.domain.ExportacaoStatus
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class ExportacaoKindResponse(
    val kind: String,
    val titulo: String,
)

data class ExportacaoJobResponse(
    val id: UUID,
    val kind: String,
    val titulo: String,
    val status: String,
    val nomeArquivo: String?,
    val expiresAt: OffsetDateTime?,
    val mensagem: String?,
    val createdAt: OffsetDateTime,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun de(job: ExportacaoJob): ExportacaoJobResponse {
            val links = linkedMapOf("self" to "/exportacoes/${job.id}")
            if (job.status == ExportacaoStatus.PRONTO && !job.storageKey.isNullOrBlank()) {
                links["download"] = "/exportacoes/${job.id}/download"
            }
            return ExportacaoJobResponse(
                id = job.id,
                kind = job.kind,
                titulo = ExportacaoKinds.titulo(job.kind),
                status = job.status,
                nomeArquivo = job.nomeArquivo,
                expiresAt = job.expiresAt,
                mensagem = job.mensagem,
                createdAt = job.createdAt,
                links = links,
            )
        }
    }
}

data class DownloadUrlResponse(
    val url: String,
    val expiresInSeconds: Long,
)
