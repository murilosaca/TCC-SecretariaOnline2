package br.ufpr.sept.so2.modules.atendimentos.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class AtendimentoResponse(
    val id: UUID,
    val alunoId: UUID,
    val categoriaId: UUID,
    val categoria: String?,
    val assunto: String,
    val resposta: String,
    val estado: String,
    val registradoEm: OffsetDateTime,
    val cienciaEm: OffsetDateTime?,
    val temAnexo: Boolean,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)

data class CategoriaAtendimentoResponse(
    val id: UUID,
    val nome: String,
)

data class CategoriasAtendimentoResponse(
    val content: List<CategoriaAtendimentoResponse>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)

data class AnexoAtendimentoResponse(
    val nomeArquivo: String,
    val downloadUrl: String,
    val expiresInSeconds: Long,
)
