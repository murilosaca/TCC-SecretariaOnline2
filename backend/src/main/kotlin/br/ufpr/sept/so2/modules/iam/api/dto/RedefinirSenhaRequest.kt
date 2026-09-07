package br.ufpr.sept.so2.modules.iam.api.dto

import jakarta.validation.constraints.NotBlank

data class RedefinirSenhaRequest(
    @field:NotBlank(message = "Token obrigatório")
    val token: String,
    @field:NotBlank(message = "Informe a nova senha")
    val novaSenha: String,
)
