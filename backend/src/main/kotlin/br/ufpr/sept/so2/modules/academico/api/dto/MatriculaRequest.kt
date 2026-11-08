package br.ufpr.sept.so2.modules.academico.api.dto

import jakarta.validation.constraints.NotEmpty
import java.util.UUID

data class MatriculaLoteRequest(
    @field:NotEmpty val disciplinaIds: List<UUID> = emptyList(),
)

data class MatriculaLoteResponse(
    val periodoId: UUID,
    val disciplinaIds: List<UUID>,
)

data class MatriculaOpcoesResponse(
    val periodoId: UUID?,
    val periodoRotulo: String?,
    val disciplinas: List<DisciplinaOpcaoResponse>,
)

data class DisciplinaOpcaoResponse(
    val id: UUID,
    val codigo: String,
    val nome: String,
    val vagas: Int,
)
