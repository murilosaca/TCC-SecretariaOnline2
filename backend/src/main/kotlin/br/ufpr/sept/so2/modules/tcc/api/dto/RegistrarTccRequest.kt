package br.ufpr.sept.so2.modules.tcc.api.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.util.UUID

data class RegistrarTccRequest(
    @field:NotNull val alunoId: UUID? = null,
    @field:NotBlank @field:Size(min = 3, max = 200) val titulo: String? = null,
    @field:NotNull val dataDefesa: LocalDate? = null,
    @field:NotNull val dataEntrega: LocalDate? = null,
    @field:NotEmpty @field:Valid val membros: List<MembroBancaRequest>? = null,
)

data class MembroBancaRequest(
    @field:NotNull val idUsuario: UUID? = null,
    @field:NotBlank val papel: String? = null,
)
