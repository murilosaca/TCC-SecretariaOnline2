package br.ufpr.sept.so2.modules.academico.api.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class PeriodoLetivoRequest(
    @field:NotNull @field:Min(2000) @field:Max(2100) val ano: Int? = null,
    @field:NotNull @field:Min(1) @field:Max(2) val semestre: Int? = null,
    @field:NotNull val inicio: LocalDate? = null,
    @field:NotNull val fim: LocalDate? = null,
    val ativo: Boolean? = null,
)
