package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "authority")
class AuthorityJpaEntity {
    @Id
    @Column(nullable = false, length = 80)
    var nome: String? = null

    @Column(nullable = false, length = 300)
    var descricao: String? = null

    @Column(nullable = false, length = 40)
    var modulo: String? = null

    @Column(nullable = false)
    var sistema: Boolean = true

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null

    @PrePersist
    fun onCreate() {
        val agora = OffsetDateTime.now()
        if (createdAt == null) {
            createdAt = agora
        }
        updatedAt = agora
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}
