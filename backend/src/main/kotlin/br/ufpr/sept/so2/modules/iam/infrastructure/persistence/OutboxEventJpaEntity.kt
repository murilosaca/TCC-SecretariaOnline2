package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

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

    constructor(tipo: String, payload: String) : this() {
        this.tipo = tipo
        this.payload = payload
        this.status = "PENDING"
        this.tentativas = 0
    }
}
