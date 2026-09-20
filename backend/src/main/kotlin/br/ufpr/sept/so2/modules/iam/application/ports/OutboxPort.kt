package br.ufpr.sept.so2.modules.iam.application.ports

import java.time.OffsetDateTime
import java.util.UUID

interface OutboxPort {
    fun enqueue(tipo: String, payload: String)

    fun claimPending(limit: Int, staleBefore: OffsetDateTime): List<OutboxClaim>

    fun markSent(id: UUID)

    fun markFailed(id: UUID, tentativas: Int, lastError: String?)

    fun markPendingRetry(id: UUID, tentativas: Int, lastError: String?)
}
