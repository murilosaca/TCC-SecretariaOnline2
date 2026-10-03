package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.domain.Persistable
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "notificacao_preferencia")
class NotificacaoPreferenciaJpaEntity : Persistable<UUID> {
    @Id
    @Column(name = "usuario_id", nullable = false)
    var usuarioId: UUID? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var canais: String? = null

    @Column(name = "dnd_inicio")
    var dndInicio: LocalTime? = null

    @Column(name = "dnd_fim")
    var dndFim: LocalTime? = null

    @Column(nullable = false, length = 20)
    var digest: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null

    @Transient
    private var novo: Boolean = true

    override fun getId(): UUID? = usuarioId

    override fun isNew(): Boolean = novo

    @PostLoad
    @PostPersist
    fun marcarPersistido() {
        novo = false
    }
}
