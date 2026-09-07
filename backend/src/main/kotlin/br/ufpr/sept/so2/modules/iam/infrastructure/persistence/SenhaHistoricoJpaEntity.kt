package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "senha_historico")
class SenhaHistoricoJpaEntity(
    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    var id: UUID? = null,
    @Column(name = "usuario_id", nullable = false)
    var usuarioId: UUID? = null,
    @Column(name = "senha_hash", nullable = false, length = 255)
    var senhaHash: String? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null,
)
