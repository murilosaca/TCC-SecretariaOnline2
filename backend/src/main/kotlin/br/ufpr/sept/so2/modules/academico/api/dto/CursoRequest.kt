package br.ufpr.sept.so2.modules.academico.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CursoRequest(
    @field:NotBlank @field:Size(max = 200) val nome: String? = null,
    @field:NotBlank @field:Size(max = 20) val sigla: String? = null,
    @field:NotBlank @field:Size(max = 30) val codigo: String? = null,
    val idCoordenador: UUID? = null,
    @field:Min(0) val horasFormativasMinimas: Int? = null,
    val ativo: Boolean? = null,
)
