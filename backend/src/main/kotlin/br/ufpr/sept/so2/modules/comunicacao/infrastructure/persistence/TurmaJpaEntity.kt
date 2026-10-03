package br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence

import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "turma",
    uniqueConstraints = [UniqueConstraint(name = "uk_turma_professor_codigo", columnNames = ["id_professor", "codigo"])],
)
class TurmaJpaEntity : BaseEntity() {
    @Column(name = "id_curso", nullable = false)
    var cursoId: UUID? = null

    @Column(name = "id_professor", nullable = false)
    var professorId: UUID? = null

    @Column(nullable = false, length = 30)
    var codigo: String = ""

    @Column(nullable = false, length = 200)
    var nome: String = ""
}
