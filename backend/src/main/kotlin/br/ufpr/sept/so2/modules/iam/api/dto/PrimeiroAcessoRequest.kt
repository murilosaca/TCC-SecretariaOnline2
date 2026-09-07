package br.ufpr.sept.so2.modules.iam.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PrimeiroAcessoRequest(
    @field:NotBlank(message = "Informe a nova senha")
    val novaSenha: String,
    @field:NotNull(message = "Aceite a política de privacidade")
    val aceiteTermos: Boolean?,
)
