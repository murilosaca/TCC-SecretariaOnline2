package br.ufpr.sept.so2.modules.formativas.infrastructure.persistence

import br.ufpr.sept.so2.modules.formativas.domain.TipoAtividadeFormativa
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "tipo_atividade_formativa",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_tipo_atividade_curso_nome", columnNames = ["id_curso", "nome"]),
    ],
)
class TipoAtividadeFormativaJpaEntity : BaseEntity() {
    @Column(name = "id_curso", nullable = false)
    var cursoId: UUID? = null

    @Column(nullable = false, length = 200)
    var nome: String = ""

    @Column(nullable = false)
    var ativo: Boolean = true

    fun toDomain(): TipoAtividadeFormativa = TipoAtividadeFormativa(
        requireNotNull(id),
        requireNotNull(cursoId),
        nome,
        ativo,
        requireNotNull(createdAt),
        requireNotNull(updatedAt),
    )

    companion object {
        fun fromDomain(tipo: TipoAtividadeFormativa) = TipoAtividadeFormativaJpaEntity().apply {
            id = tipo.id
            cursoId = tipo.cursoId
            nome = tipo.nome
            ativo = tipo.ativo
        }
    }
}
