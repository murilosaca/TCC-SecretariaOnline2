package br.ufpr.sept.so2.modules.diplomas.infrastructure.persistence

import br.ufpr.sept.so2.modules.diplomas.domain.Diploma
import br.ufpr.sept.so2.modules.diplomas.domain.DiplomaSituacao
import br.ufpr.sept.so2.modules.diplomas.domain.MetodoEntregaDiploma
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "diploma",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_diploma_aluno", columnNames = ["id_aluno"]),
        UniqueConstraint(name = "uq_diploma_numero", columnNames = ["numero"]),
    ],
)
class DiplomaJpaEntity : BaseEntity() {

    @Column(name = "id_aluno", nullable = false)
    var idAluno: UUID? = null

    @Column(name = "id_curso", nullable = false)
    var idCurso: UUID? = null

    @Column(name = "id_periodo_letivo")
    var idPeriodoLetivo: UUID? = null

    @Column(nullable = false, length = 40)
    var numero: String? = null

    @Column(nullable = false, length = 20)
    var situacao: String? = null

    @Column(name = "data_colacao", nullable = false)
    var dataColacao: OffsetDateTime? = null

    @Column(nullable = false, length = 40)
    var livro: String? = null

    @Column(nullable = false, length = 40)
    var folha: String? = null

    @Column(length = 80)
    var turma: String? = null

    @Column(name = "storage_key", length = 512)
    var storageKey: String? = null

    @Column(name = "metodo_entrega", length = 20)
    var metodoEntrega: String? = null

    @Column(name = "data_entrega")
    var dataEntrega: OffsetDateTime? = null

    fun merge(diploma: Diploma) {
        idAluno = diploma.idAluno
        idCurso = diploma.idCurso
        idPeriodoLetivo = diploma.idPeriodoLetivo
        numero = diploma.numero
        situacao = diploma.situacao.name
        dataColacao = diploma.dataColacao
        livro = diploma.livro
        folha = diploma.folha
        turma = diploma.turma
        storageKey = diploma.storageKey
        metodoEntrega = diploma.metodoEntrega?.name
        dataEntrega = diploma.dataEntrega
    }

    fun toDomain(): Diploma = Diploma(
        id!!,
        idAluno!!,
        idCurso!!,
        idPeriodoLetivo,
        numero!!,
        DiplomaSituacao.from(situacao),
        dataColacao!!,
        livro!!,
        folha!!,
        turma,
        storageKey,
        metodoEntrega?.let { MetodoEntregaDiploma.from(it) },
        dataEntrega,
        createdAt!!,
        updatedAt!!,
    )

    companion object {
        fun fromDomain(diploma: Diploma): DiplomaJpaEntity {
            val entity = DiplomaJpaEntity()
            entity.id = diploma.id
            entity.merge(diploma)
            return entity
        }
    }
}
