package br.ufpr.sept.so2.modules.tcc.infrastructure.persistence

import br.ufpr.sept.so2.modules.tcc.domain.MembroBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.PapelBancaTcc
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "tcc_membro",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_tcc_membro_usuario", columnNames = ["id_tcc", "id_usuario"]),
    ],
)
class TccMembroJpaEntity : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tcc", nullable = false)
    var tcc: TccJpaEntity? = null

    @Column(name = "id_usuario", nullable = false)
    var idUsuario: UUID? = null

    @Column(nullable = false, length = 20)
    var papel: String? = null

    fun merge(membro: MembroBancaTcc, agora: OffsetDateTime) {
        idUsuario = membro.idUsuario
        papel = membro.papel.name
        if (createdAt == null) {
            createdAt = agora
            updatedAt = agora
        }
    }

    fun toDomain(): MembroBancaTcc = MembroBancaTcc(id!!, idUsuario!!, PapelBancaTcc.from(papel))
}
