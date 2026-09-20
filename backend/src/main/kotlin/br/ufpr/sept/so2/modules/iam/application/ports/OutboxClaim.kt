package br.ufpr.sept.so2.modules.iam.application.ports

import java.util.UUID

data class OutboxClaim(
    val id: UUID,
    val tipo: String,
    val payload: String,
    val tentativas: Int,
)
