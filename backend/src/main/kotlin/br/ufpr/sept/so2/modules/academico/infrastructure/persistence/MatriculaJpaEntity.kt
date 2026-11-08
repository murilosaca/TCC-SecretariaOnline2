package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.domain.Matricula
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "matricula",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["id_aluno", "id_disciplina", "id_periodo_letivo"]),
    ],
)
class MatriculaJpaEntity : BaseEntity() {
    @Column(name = "id_aluno", nullable = false)
    var idAluno: UUID? = null

    @Column(name = "id_disciplina", nullable = false)
    var idDisciplina: UUID? = null

    @Column(name = "id_periodo_letivo", nullable = false)
    var idPeriodoLetivo: UUID? = null

    fun toDomain() = Matricula(
        requireNotNull(id),
        requireNotNull(idAluno),
        requireNotNull(idDisciplina),
        requireNotNull(idPeriodoLetivo),
        requireNotNull(createdAt),
    )

    companion object {
        fun fromDomain(matricula: Matricula) = MatriculaJpaEntity().apply {
            id = matricula.id
            idAluno = matricula.idAluno
            idDisciplina = matricula.idDisciplina
            idPeriodoLetivo = matricula.idPeriodoLetivo
        }
    }
}
