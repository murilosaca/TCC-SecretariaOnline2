package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "tipo_solicitacao_versao")
class TipoSolicitacaoVersaoJpaEntity : BaseEntity() {
    @Column(name = "tipo_id", nullable = false)
    var tipoId: UUID? = null

    @Column(nullable = false)
    var versao: Int = 0

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_schema", nullable = false, columnDefinition = "jsonb")
    var formSchema: String? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "workflow_json", nullable = false, columnDefinition = "jsonb")
    var workflowJson: String? = null

    @Column(name = "publicado_em", nullable = false)
    var publicadoEm: OffsetDateTime? = null

    @Column(name = "publicado_por")
    var publicadoPor: UUID? = null
}
