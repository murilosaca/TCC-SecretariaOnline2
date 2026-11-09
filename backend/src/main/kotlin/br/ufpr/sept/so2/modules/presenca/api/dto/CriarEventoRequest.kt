package br.ufpr.sept.so2.modules.presenca.api.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.OffsetDateTime
import java.util.UUID

data class JanelaHorario(
    @field:NotNull val inicio: OffsetDateTime,
    @field:NotNull val fim: OffsetDateTime,
)

data class CriarEventoRequest(
    @field:NotBlank val titulo: String,
    @field:NotNull val inicioEm: OffsetDateTime,
    @field:NotNull val fimEm: OffsetDateTime,
    @field:Positive val cargaHoraria: Int,
    val attendanceMode: String? = null,
    /** Curso do público-alvo. Secretaria ainda recorta pelo próprio escopo. */
    val cursoId: UUID? = null,
    @field:Valid val janelaEntrada: JanelaHorario? = null,
    @field:Valid val janelaSaida: JanelaHorario? = null,
)
