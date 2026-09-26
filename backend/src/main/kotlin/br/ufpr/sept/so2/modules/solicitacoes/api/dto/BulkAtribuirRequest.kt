package br.ufpr.sept.so2.modules.solicitacoes.api.dto

import java.util.UUID

data class BulkAtribuirRequest(
    val ids: List<UUID>,
    val deliberadorId: UUID,
)

data class DeliberadorOpcaoResponse(
    val id: UUID,
    val nome: String,
)
