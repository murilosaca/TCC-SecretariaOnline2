package br.ufpr.sept.so2.modules.formativas.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class FormativaResponse(
    val id: UUID,
    val idAluno: UUID,
    val idEvento: UUID?,
    val origem: String,
    val titulo: String,
    val cargaHoraria: Int,
    val estado: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)
