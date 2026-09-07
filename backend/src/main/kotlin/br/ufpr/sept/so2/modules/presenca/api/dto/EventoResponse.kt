package br.ufpr.sept.so2.modules.presenca.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class EventoResponse(
    val id: UUID,
    val idAnfitriao: UUID?,
    val titulo: String,
    val inicioEm: OffsetDateTime,
    val fimEm: OffsetDateTime,
    val cargaHoraria: Int,
    val attendanceMode: String,
    val estado: String,
    val situacaoPresenca: String,
    val janelaAtiva: Boolean,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)
