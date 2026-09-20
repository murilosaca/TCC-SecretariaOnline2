package br.ufpr.sept.so2.modules.certificados.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class CertificadoResponse(
    val id: UUID,
    val tipo: String,
    val titulo: String,
    val cargaHoraria: Int,
    val emitidoEm: OffsetDateTime,
    val hashSha256: String,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)
