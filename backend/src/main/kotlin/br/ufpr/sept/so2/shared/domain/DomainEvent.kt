package br.ufpr.sept.so2.shared.domain

import java.time.OffsetDateTime
import java.util.UUID

interface DomainEvent {
    fun eventId(): UUID
    fun occurredAt(): OffsetDateTime
    fun type(): String
}
