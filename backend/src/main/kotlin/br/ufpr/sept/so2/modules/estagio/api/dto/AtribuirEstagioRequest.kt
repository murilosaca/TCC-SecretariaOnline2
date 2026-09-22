package br.ufpr.sept.so2.modules.estagio.api.dto

import java.util.UUID

data class AtribuirEstagioRequest(
    val estagioId: UUID,
    val assigneeId: UUID,
)
