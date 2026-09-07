package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.domain.Disciplina
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "disciplina")
class DisciplinaJpaEntity : BaseEntity() {
    @Column(name = "id_curso", nullable = false)
    var idCurso: UUID? = null

    @Column(nullable = false, length = 20)
    var codigo: String = ""

    @Column(nullable = false, length = 200)
    var nome: String = ""

    @Column(nullable = false)
    var periodo: Int = 0

    @Column(name = "carga_horaria_total", nullable = false)
    var cargaHorariaTotal: Int = 0

    @Column(nullable = false)
    var creditos: Int = 0

    @Column(nullable = false)
    var ativa: Boolean = false

    fun merge(disciplina: Disciplina) {
        codigo = disciplina.codigo
        nome = disciplina.nome
        periodo = disciplina.periodo
        cargaHorariaTotal = disciplina.cargaHorariaTotal
        creditos = disciplina.creditos
        ativa = disciplina.ativa
    }

    fun toDomain() = Disciplina(
        requireNotNull(id),
        requireNotNull(idCurso),
        codigo,
        nome,
        periodo,
        cargaHorariaTotal,
        creditos,
        ativa,
        requireNotNull(createdAt),
        requireNotNull(updatedAt),
    )

    companion object {
        fun fromDomain(disciplina: Disciplina) = DisciplinaJpaEntity().apply {
            id = disciplina.id
            idCurso = disciplina.idCurso
            codigo = disciplina.codigo
            nome = disciplina.nome
            periodo = disciplina.periodo
            cargaHorariaTotal = disciplina.cargaHorariaTotal
            creditos = disciplina.creditos
            ativa = disciplina.ativa
        }
    }
}
