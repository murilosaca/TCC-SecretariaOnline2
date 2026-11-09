package br.ufpr.sept.so2.modules.academico.api.dto

import jakarta.validation.constraints.NotNull

data class AtivoCursoRequest(
    @field:NotNull
    val ativo: Boolean?,
)
