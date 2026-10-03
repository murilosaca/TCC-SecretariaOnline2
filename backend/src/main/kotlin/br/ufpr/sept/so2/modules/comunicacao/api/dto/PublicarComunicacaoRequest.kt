package br.ufpr.sept.so2.modules.comunicacao.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class AudienciaRequest(
    val tipo: String? = null,
    val id: UUID? = null,
)

data class PublicarComunicacaoRequest(
    val titulo: String? = null,
    val corpo: String? = null,
    val audiencia: AudienciaRequest? = null,
    val prioridade: String? = null,
    @param:JsonProperty("expiraEm")
    val expiraEm: OffsetDateTime? = null,
)
