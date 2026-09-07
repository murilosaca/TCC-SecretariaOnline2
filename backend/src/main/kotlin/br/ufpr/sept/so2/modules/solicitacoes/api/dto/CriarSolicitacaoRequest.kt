package br.ufpr.sept.so2.modules.solicitacoes.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CriarSolicitacaoRequest(
    @field:NotBlank
    val tipoCodigo: String,
    @field:NotNull
    val payload: Map<String, Any?>,
)
