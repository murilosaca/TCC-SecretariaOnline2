package br.ufpr.sept.so2.modules.iam.api.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Informe e-mail ou GRR")
    val identificador: String,
    @field:NotBlank(message = "Informe a senha")
    val senha: String,
)
