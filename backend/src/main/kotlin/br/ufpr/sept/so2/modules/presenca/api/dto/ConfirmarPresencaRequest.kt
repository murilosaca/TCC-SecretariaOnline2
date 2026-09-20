package br.ufpr.sept.so2.modules.presenca.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ConfirmarPresencaRequest(
    val pin: String? = null,
    val token: String? = null,
    @field:NotBlank @field:Size(min = 8, max = 64) val deviceUuid: String,
    @field:NotBlank val fase: String,
)
