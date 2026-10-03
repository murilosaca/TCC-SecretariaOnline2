package br.ufpr.sept.so2.modules.presenca.api.dto

import jakarta.validation.constraints.Positive
import java.time.OffsetDateTime
import java.util.UUID

/** Campos ausentes mantêm o valor atual (F5.14). */
data class AtualizarEventoRequest(
    val titulo: String? = null,
    val inicioEm: OffsetDateTime? = null,
    val fimEm: OffsetDateTime? = null,
    @field:Positive val cargaHoraria: Int? = null,
    val attendanceMode: String? = null,
    val cursoId: UUID? = null,
)
