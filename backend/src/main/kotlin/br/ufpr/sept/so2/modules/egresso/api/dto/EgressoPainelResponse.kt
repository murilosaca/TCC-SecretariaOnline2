package br.ufpr.sept.so2.modules.egresso.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.ALWAYS)
data class EgressoPainelResponse(
    val nome: String,
    val curso: String?,
    val concluidoEm: OffsetDateTime?,
    val kpis: KpisResponse,
    val diploma: DiplomaResponse?,
    val colacao: ColacaoResponse?,
    val certificados: List<CertificadoItemResponse>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class KpisResponse(
        val horasFormativasValidadas: Int,
        val certificadosEmitidos: Int,
        val situacaoDiploma: String?,
    )

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class DiplomaResponse(
        val numero: String,
        val emitidoEm: OffsetDateTime,
        @get:JsonProperty("_links")
        val links: Map<String, String>,
    )

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class ColacaoResponse(
        val data: OffsetDateTime?,
        val turma: String?,
    )

    @JsonInclude(JsonInclude.Include.ALWAYS)
    data class CertificadoItemResponse(
        val id: UUID,
        val titulo: String,
        val tipo: String,
        val emitidoEm: OffsetDateTime,
        val hashSha256: String,
        @get:JsonProperty("_links")
        val links: Map<String, String>,
    )
}
