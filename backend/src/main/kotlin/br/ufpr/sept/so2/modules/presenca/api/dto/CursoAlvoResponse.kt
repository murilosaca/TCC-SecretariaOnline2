package br.ufpr.sept.so2.modules.presenca.api.dto

import java.util.UUID

data class CursoAlvoResponse(
    val id: UUID,
    val sigla: String,
    val nome: String,
)
