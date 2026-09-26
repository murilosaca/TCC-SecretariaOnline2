package br.ufpr.sept.so2.modules.estagio.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.util.UUID

data class RegistrarEstagioRequest(
    @field:NotNull val alunoId: UUID? = null,
    @field:NotBlank @field:Size(max = 200) val empresa: String? = null,
    @field:NotBlank @field:Size(max = 200) val supervisor: String? = null,
    @field:NotNull val inicio: LocalDate? = null,
    @field:NotNull val fim: LocalDate? = null,
)
