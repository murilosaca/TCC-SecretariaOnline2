package br.ufpr.sept.so2.shared.infrastructure.persistence

import br.ufpr.sept.so2.shared.infrastructure.Uuids
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.OffsetDateTime
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity {
    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    var id: UUID? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
        protected set

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
        protected set

    protected constructor()

    protected constructor(id: UUID?) {
        this.id = id
    }

    @PrePersist
    protected fun onCreate() {
        if (id == null) {
            id = Uuids.v7()
        }
        val now = OffsetDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    protected fun onUpdate() {
        updatedAt = OffsetDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as BaseEntity
        return id != null && id == that.id
    }

    override fun hashCode(): Int = id.hashCode()
}
