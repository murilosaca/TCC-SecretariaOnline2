package br.ufpr.sept.so2.modules.atendimentos.infrastructure.persistence

import br.ufpr.sept.so2.modules.atendimentos.domain.CategoriaAtendimento
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "atendimento_categoria")
class CategoriaAtendimentoJpaEntity : BaseEntity() {

    @Column(nullable = false, length = 120)
    var nome: String? = null

    @Column(nullable = false)
    var ativo: Boolean = true

    fun merge(categoria: CategoriaAtendimento) {
        nome = categoria.nome
        ativo = categoria.ativo
    }

    fun toDomain(): CategoriaAtendimento = CategoriaAtendimento(id!!, nome!!, ativo, createdAt!!, updatedAt!!)

    companion object {
        fun fromDomain(categoria: CategoriaAtendimento): CategoriaAtendimentoJpaEntity {
            val entity = CategoriaAtendimentoJpaEntity()
            entity.id = categoria.id
            entity.merge(categoria)
            return entity
        }
    }
}
