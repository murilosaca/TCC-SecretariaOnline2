package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "curso")
class CursoJpaEntity : BaseEntity() {
    @Column(nullable = false, length = 200)
    var nome: String = ""

    @Column(nullable = false, unique = true, length = 20)
    var sigla: String = ""

    @Column(nullable = false, unique = true, length = 30)
    var codigo: String = ""

    @Column(name = "id_coordenador")
    var idCoordenador: UUID? = null

    @Column(name = "horas_formativas_minimas", nullable = false)
    var horasFormativasMinimas: Int = 0

    @Column(nullable = false)
    var ativo: Boolean = false

    fun merge(curso: Curso) {
        nome = curso.nome
        sigla = curso.sigla
        codigo = curso.codigo
        idCoordenador = curso.idCoordenador
        horasFormativasMinimas = curso.horasFormativasMinimas
        ativo = curso.ativo
    }

    fun toDomain() = Curso(
        requireNotNull(id),
        nome,
        sigla,
        codigo,
        idCoordenador,
        horasFormativasMinimas,
        ativo,
        requireNotNull(createdAt),
        requireNotNull(updatedAt),
    )

    companion object {
        fun fromDomain(curso: Curso) = CursoJpaEntity().apply {
            id = curso.id
            nome = curso.nome
            sigla = curso.sigla
            codigo = curso.codigo
            idCoordenador = curso.idCoordenador
            horasFormativasMinimas = curso.horasFormativasMinimas
            ativo = curso.ativo
        }
    }
}
