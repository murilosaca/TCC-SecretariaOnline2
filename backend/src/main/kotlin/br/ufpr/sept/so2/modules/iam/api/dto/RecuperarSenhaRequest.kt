package br.ufpr.sept.so2.modules.iam.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class RecuperarSenhaRequest(
    @field:NotBlank(message = "Informe um e-mail")
    @field:Email(message = "Informe um e-mail válido")
    val email: String,
)
