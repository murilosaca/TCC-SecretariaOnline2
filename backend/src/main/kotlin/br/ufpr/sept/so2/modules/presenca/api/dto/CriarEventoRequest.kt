package br.ufpr.sept.so2.modules.presenca.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.OffsetDateTime
import java.util.UUID

data class CriarEventoRequest(
    @field:NotBlank val titulo: String,
    @field:NotNull val inicioEm: OffsetDateTime,
    @field:NotNull val fimEm: OffsetDateTime,
    @field:Positive val cargaHoraria: Int,
    val attendanceMode: String? = null,
    /** Obrigatório para quem tem `event.view_curso`; o professor não informa curso. */
    val cursoId: UUID? = null,
)
