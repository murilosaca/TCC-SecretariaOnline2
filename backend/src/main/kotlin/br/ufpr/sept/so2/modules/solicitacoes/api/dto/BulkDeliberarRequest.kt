package br.ufpr.sept.so2.modules.solicitacoes.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class BulkDeliberarRequest(
    @field:NotEmpty
    val ids: List<UUID>,
    @field:NotBlank
    val decisao: String,
    val justificativa: String? = null,
)
