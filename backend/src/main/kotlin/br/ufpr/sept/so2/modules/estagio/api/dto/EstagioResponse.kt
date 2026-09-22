package br.ufpr.sept.so2.modules.estagio.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class EstagioResponse(
    val id: UUID,
    val idAluno: UUID,
    val alunoNome: String?,
    val idCurso: UUID,
    val idOrientador: UUID?,
    val orientadorRotulo: String?,
    val empresa: String,
    val supervisor: String,
    val inicio: LocalDate,
    val fim: LocalDate,
    val situacao: String,
    val documentoPendente: String?,
    val documentos: List<DocumentoEstagioResponse>,
    val pareceres: List<ParecerEstagioResponse>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)

data class DocumentoEstagioResponse(
    val id: UUID,
    val tipo: String,
    val obrigatorio: Boolean,
    val estado: String,
    val nomeArquivo: String?,
    val tamanho: Int?,
    val enviadoEm: OffsetDateTime?,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)

data class ParecerEstagioResponse(
    val id: UUID,
    val idDocumento: UUID,
    val tipoDocumento: String,
    val idAutor: UUID,
    val autorRotulo: String?,
    val acao: String,
    val texto: String,
    val createdAt: OffsetDateTime,
)
