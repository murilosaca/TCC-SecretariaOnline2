package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "presenca_dispositivo")
@IdClass(PresencaDispositivoId::class)
class PresencaDispositivoJpaEntity {
    @Id
    @Column(name = "evento_id", nullable = false, columnDefinition = "uuid")
    var eventoId: UUID? = null

    @Id
    @Column(name = "device_uuid", nullable = false, length = 64)
    var deviceUuid: String? = null

    @Column(name = "usuario_id", nullable = false, columnDefinition = "uuid")
    var usuarioId: UUID? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null
}

data class PresencaDispositivoId(
    var eventoId: UUID? = null,
    var deviceUuid: String? = null,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
