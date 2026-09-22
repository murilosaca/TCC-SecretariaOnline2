package br.ufpr.sept.so2.modules.estagio.infrastructure.persistence

import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.EstagioSituacao
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "estagio")
class EstagioJpaEntity : BaseEntity() {

    @Column(name = "id_aluno", nullable = false)
    var idAluno: UUID? = null

    @Column(name = "id_curso", nullable = false)
    var idCurso: UUID? = null

    @Column(name = "id_orientador")
    var idOrientador: UUID? = null

    @Column(nullable = false, length = 200)
    var empresa: String? = null

    @Column(nullable = false, length = 200)
    var supervisor: String? = null

    @Column(nullable = false)
    var inicio: LocalDate? = null

    @Column(nullable = false)
    var fim: LocalDate? = null

    @Column(nullable = false, length = 20)
    var situacao: String? = null

    @OneToMany(mappedBy = "estagio", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    var documentos: MutableList<EstagioDocumentoJpaEntity> = mutableListOf()

    fun merge(estagio: Estagio) {
        idAluno = estagio.idAluno
        idCurso = estagio.idCurso
        idOrientador = estagio.idOrientador
        empresa = estagio.empresa
        supervisor = estagio.supervisor
        inicio = estagio.inicio
        fim = estagio.fim
        situacao = estagio.situacao.name
        if (createdAt == null) {
            createdAt = estagio.createdAt
        }
        updatedAt = estagio.updatedAt
        estagio.documentos.forEach { documento ->
            val entity = documentos.find { it.id == documento.id } ?: EstagioDocumentoJpaEntity().also {
                it.id = documento.id
                it.estagio = this
                documentos.add(it)
            }
            entity.merge(documento)
        }
    }

    fun toDomain(): Estagio = Estagio(
        id!!,
        idAluno!!,
        idCurso!!,
        idOrientador,
        empresa!!,
        supervisor!!,
        inicio!!,
        fim!!,
        EstagioSituacao.from(situacao),
        createdAt!!,
        updatedAt!!,
        documentos.map { it.toDomain() }.sortedBy { it.tipo.ordinal },
    )

    companion object {
        fun fromDomain(estagio: Estagio): EstagioJpaEntity {
            val entity = EstagioJpaEntity()
            entity.id = estagio.id
            entity.merge(estagio)
            return entity
        }
    }
}
