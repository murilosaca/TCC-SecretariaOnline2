package br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.util.Objects
import java.util.UUID

@Entity
@Table(name = "turma_aluno")
@IdClass(TurmaAlunoId::class)
class TurmaAlunoJpaEntity {
    @Id
    @Column(name = "id_turma", nullable = false)
    var idTurma: UUID? = null

    @Id
    @Column(name = "id_aluno", nullable = false)
    var idAluno: UUID? = null
}

class TurmaAlunoId : Serializable {
    var idTurma: UUID? = null
    var idAluno: UUID? = null

    constructor()

    constructor(idTurma: UUID?, idAluno: UUID?) {
        this.idTurma = idTurma
        this.idAluno = idAluno
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is TurmaAlunoId) {
            return false
        }
        return idTurma == other.idTurma && idAluno == other.idAluno
    }

    override fun hashCode(): Int = Objects.hash(idTurma, idAluno)

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
