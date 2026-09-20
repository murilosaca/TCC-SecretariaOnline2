package br.ufpr.sept.so2.modules.solicitacoes.api.dto

import jakarta.validation.constraints.NotBlank

data class TransicionarSolicitacaoRequest(
    @field:NotBlank
    val action: String,
    val parecer: String? = null,
)
