package br.ufpr.sept.so2.modules.iam.domain

import java.time.OffsetDateTime
import java.util.UUID

class RefreshSessao(
    val id: UUID,
    val usuarioId: UUID,
    val tokenHash: String,
    val expiresAt: OffsetDateTime,
    var used: Boolean,
    var revoked: Boolean,
    val createdAt: OffsetDateTime,
    var updatedAt: OffsetDateTime,
) {
    fun expirada(agora: OffsetDateTime): Boolean = !expiresAt.isAfter(agora)

    fun reutilizadaOuRevogada(): Boolean = used || revoked

    fun marcarUsada(agora: OffsetDateTime) {
        used = true
        updatedAt = agora
    }

    fun revogar(agora: OffsetDateTime) {
        revoked = true
        updatedAt = agora
    }
}
