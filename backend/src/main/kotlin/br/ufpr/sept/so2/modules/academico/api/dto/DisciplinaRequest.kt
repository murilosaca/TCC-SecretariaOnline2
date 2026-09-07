package br.ufpr.sept.so2.modules.academico.api.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.util.UUID

data class DisciplinaRequest(
    @field:NotNull val idCurso: UUID? = null,
    @field:NotBlank @field:Size(max = 20) val codigo: String? = null,
    @field:NotBlank @field:Size(max = 200) val nome: String? = null,
    @field:Min(1) @field:Max(20) val periodo: Int? = null,
    @field:Positive val cargaHorariaTotal: Int? = null,
    @field:Positive val creditos: Int? = null,
    val ativa: Boolean? = null,
)
