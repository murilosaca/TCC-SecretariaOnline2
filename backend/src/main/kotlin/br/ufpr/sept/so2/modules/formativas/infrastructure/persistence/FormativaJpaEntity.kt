package br.ufpr.sept.so2.modules.formativas.infrastructure.persistence

import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import br.ufpr.sept.so2.modules.formativas.domain.FormativaOrigem
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "formativa",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_formativa_evento_aluno", columnNames = ["id_evento", "id_aluno"]),
    ],
)
class FormativaJpaEntity : BaseEntity() {

    @Column(name = "id_aluno", nullable = false)
    var idAluno: UUID? = null

    @Column(name = "id_evento")
    var idEvento: UUID? = null

    @Column(nullable = false, length = 30)
    var origem: String? = null

    @Column(nullable = false, length = 200)
    var titulo: String? = null

    @Column(name = "carga_horaria", nullable = false)
    var cargaHoraria: Int = 0

    @Column(nullable = false, length = 40)
    var estado: String? = null

    @Column(length = 2000)
    var parecer: String? = null

    @Column(name = "id_revisor")
    var idRevisor: UUID? = null

    @Column(name = "reviewed_at")
    var reviewedAt: OffsetDateTime? = null

    @Column(name = "id_responsavel")
    var idResponsavel: UUID? = null

    fun merge(formativa: Formativa) {
        idAluno = formativa.idAluno
        idEvento = formativa.idEvento
        origem = formativa.origem.name
        titulo = formativa.titulo
        cargaHoraria = formativa.cargaHoraria
        estado = formativa.estado.name
        parecer = formativa.parecer
        idRevisor = formativa.idRevisor
        reviewedAt = formativa.reviewedAt
        idResponsavel = formativa.idResponsavel
    }

    fun toDomain(): Formativa = Formativa(
        id!!,
        idAluno!!,
        idEvento,
        FormativaOrigem.from(origem),
        titulo!!,
        cargaHoraria,
        FormativaEstado.from(estado),
        createdAt!!,
        updatedAt!!,
        parecer,
        idRevisor,
        reviewedAt,
        idResponsavel,
    )

    companion object {
        fun fromDomain(formativa: Formativa): FormativaJpaEntity {
            val entity = FormativaJpaEntity()
            entity.id = formativa.id
            entity.merge(formativa)
            return entity
        }
    }
}
