package br.ufpr.sept.so2.modules.iam.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AdminUsuarioRequest(
    @field:NotBlank @field:Size(max = 200) val nome: String? = null,
    @field:NotBlank @field:Size(max = 254) val emailInstitucional: String? = null,
    @field:Size(max = 254) val emailPessoal: String? = null,
    @field:Size(max = 11) val grr: String? = null,
)
