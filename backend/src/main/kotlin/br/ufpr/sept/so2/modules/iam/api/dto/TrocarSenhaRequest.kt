package br.ufpr.sept.so2.modules.iam.api.dto

import jakarta.validation.constraints.NotBlank

data class TrocarSenhaRequest(
    @field:NotBlank(message = "Informe a senha atual")
    val senhaAtual: String,
    @field:NotBlank(message = "Informe a nova senha")
    val novaSenha: String,
)
