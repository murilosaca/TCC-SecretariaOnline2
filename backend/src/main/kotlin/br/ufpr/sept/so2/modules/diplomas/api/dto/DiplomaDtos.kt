package br.ufpr.sept.so2.modules.diplomas.api.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ElegivelColacaoResponse(
    val alunoId: UUID,
    val nome: String,
    val grr: String,
    val elegivel: Boolean,
    val bloqueio: BloqueioResponse?,
) {
    data class BloqueioResponse(
        val razao: String,
        val detalhe: String?,
    )
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ElegiveisColacaoResponse(
    val cursoId: UUID,
    val periodoId: UUID,
    val content: List<ElegivelColacaoResponse>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DiplomaResponse(
    val id: UUID,
    val alunoId: UUID,
    val alunoNome: String?,
    val cursoId: UUID,
    val periodoId: UUID?,
    val numero: String,
    val situacao: String,
    val dataColacao: OffsetDateTime,
    val livro: String,
    val folha: String,
    val turma: String?,
    val metodoEntrega: String?,
    val dataEntrega: OffsetDateTime?,
    val temPdf: Boolean,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)

data class ConfirmarColacaoRequest(
    val cursoId: UUID,
    val periodoId: UUID,
    val alunoIds: List<UUID>,
    val dataColacao: OffsetDateTime,
    val livro: String,
    val folha: String,
    val turma: String? = null,
)

data class ConfirmarEntregaRequest(
    val metodo: String,
    val dataEntrega: OffsetDateTime? = null,
)
