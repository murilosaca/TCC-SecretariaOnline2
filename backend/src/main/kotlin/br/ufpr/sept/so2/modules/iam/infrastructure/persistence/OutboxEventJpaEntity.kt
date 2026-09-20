package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "outbox_event")
class OutboxEventJpaEntity() : BaseEntity() {
    @Column(nullable = false, length = 80)
    var tipo: String? = null

    @Column(nullable = false, columnDefinition = "TEXT")
    var payload: String? = null

    @Column(nullable = false, length = 20)
    var status: String? = null

    @Column(nullable = false)
    var tentativas: Int = 0

    @Column(name = "last_error", length = 500)
    var lastError: String? = null

    @Column(name = "processed_at")
    var processedAt: OffsetDateTime? = null

    constructor(tipo: String, payload: String) : this() {
        this.tipo = tipo
        this.payload = payload
        this.status = STATUS_PENDING
        this.tentativas = 0
    }

    fun marcarProcessamento() {
        status = STATUS_PROCESSING
    }

    fun marcarEnviado() {
        status = STATUS_SENT
        processedAt = OffsetDateTime.now()
        lastError = null
    }

    fun marcarFalha(proximaTentativa: Int, erro: String?) {
        tentativas = proximaTentativa
        lastError = erro
        status = STATUS_FAILED
        processedAt = OffsetDateTime.now()
    }

    fun marcarRetry(proximaTentativa: Int, erro: String?) {
        tentativas = proximaTentativa
        lastError = erro
        status = STATUS_PENDING
    }

    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_PROCESSING = "PROCESSING"
        const val STATUS_SENT = "SENT"
        const val STATUS_FAILED = "FAILED"
    }
}
