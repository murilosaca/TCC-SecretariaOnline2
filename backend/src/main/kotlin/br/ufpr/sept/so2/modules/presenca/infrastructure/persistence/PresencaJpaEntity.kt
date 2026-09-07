package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence

import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.modules.presenca.domain.Presenca
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "presenca",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_presenca_evento_usuario_fase",
            columnNames = ["evento_id", "usuario_id", "fase"],
        ),
    ],
)
class PresencaJpaEntity : BaseEntity() {

    @Column(name = "evento_id", nullable = false)
    var eventoId: UUID? = null

    @Column(name = "usuario_id", nullable = false)
    var usuarioId: UUID? = null

    @Column(nullable = false, length = 20)
    var fase: String? = null

    @Column(name = "device_uuid", nullable = false, length = 64)
    var deviceUuid: String? = null

    @Column(nullable = false)
    var instante: OffsetDateTime? = null

    fun toDomain(): Presenca = Presenca(
        id!!,
        eventoId!!,
        usuarioId!!,
        FasePresenca.from(fase),
        deviceUuid!!,
        instante!!,
        createdAt!!,
        updatedAt!!,
    )

    companion object {
        fun fromDomain(presenca: Presenca): PresencaJpaEntity {
            val entity = PresencaJpaEntity()
            entity.id = presenca.id
            entity.eventoId = presenca.eventoId
            entity.usuarioId = presenca.usuarioId
            entity.fase = presenca.fase.name
            entity.deviceUuid = presenca.deviceUuid
            entity.instante = presenca.instante
            return entity
        }
    }
}
