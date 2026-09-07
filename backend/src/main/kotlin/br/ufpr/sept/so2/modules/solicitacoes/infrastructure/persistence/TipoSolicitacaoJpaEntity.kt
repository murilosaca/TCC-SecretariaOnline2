package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "tipo_solicitacao")
class TipoSolicitacaoJpaEntity protected constructor() : BaseEntity() {

    @field:Column(nullable = false, unique = true, length = 80)
    var codigo: String? = null

    @field:Column(nullable = false, length = 200)
    var nome: String? = null

    @field:Column(columnDefinition = "text")
    var descricao: String? = null

    @field:Column(nullable = false, length = 20)
    var status: String? = null

    @field:JdbcTypeCode(SqlTypes.JSON)
    @field:Column(name = "form_schema", nullable = false, columnDefinition = "jsonb")
    var formSchema: String? = null

    @field:JdbcTypeCode(SqlTypes.JSON)
    @field:Column(name = "workflow_json", nullable = false, columnDefinition = "jsonb")
    var workflowJson: String? = null

    @field:Column(name = "prazo_dias", nullable = false)
    var prazoDias: Int = 0

    @field:Column(nullable = false)
    var versao: Int = 0

    fun merge(tipo: TipoSolicitacao) {
        this.codigo = tipo.codigo
        this.nome = tipo.nome
        this.descricao = tipo.descricao
        this.status = tipo.status
        this.formSchema = tipo.formSchema
        this.workflowJson = tipo.workflowJson
        this.prazoDias = tipo.prazoDias
        this.versao = tipo.versao
    }

    fun toDomain(): TipoSolicitacao =
        TipoSolicitacao(
            id!!,
            codigo!!,
            nome!!,
            descricao,
            status!!,
            formSchema!!,
            workflowJson!!,
            prazoDias,
            versao,
            createdAt,
            updatedAt,
        )

    companion object {
        @JvmStatic
        fun fromDomain(tipo: TipoSolicitacao): TipoSolicitacaoJpaEntity {
            val entity = TipoSolicitacaoJpaEntity()
            entity.id = tipo.id
            entity.merge(tipo)
            return entity
        }
    }
}
