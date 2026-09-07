package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.modules.solicitacoes.domain.SolicitacaoEvento
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "solicitacao")
class SolicitacaoJpaEntity protected constructor() : BaseEntity() {

    @field:Column(name = "tipo_id", nullable = false)
    var tipoId: UUID? = null

    @field:Column(name = "tipo_codigo", nullable = false, length = 80)
    var tipoCodigo: String? = null

    @field:Column(name = "tipo_nome", nullable = false, length = 200)
    var tipoNome: String? = null

    @field:Column(name = "tipo_versao", nullable = false)
    var tipoVersao: Int = 0

    @field:Column(name = "solicitante_id", nullable = false)
    var solicitanteId: UUID? = null

    @field:Column(nullable = false, unique = true, length = 32)
    var protocolo: String? = null

    @field:Column(nullable = false, length = 40)
    var estado: String? = null

    @field:JdbcTypeCode(SqlTypes.JSON)
    @field:Column(nullable = false, columnDefinition = "jsonb")
    var payload: String? = null

    @field:JdbcTypeCode(SqlTypes.JSON)
    @field:Column(name = "form_schema_snapshot", nullable = false, columnDefinition = "jsonb")
    var formSchemaSnapshot: String? = null

    @field:JdbcTypeCode(SqlTypes.JSON)
    @field:Column(name = "workflow_snapshot", nullable = false, columnDefinition = "jsonb")
    var workflowSnapshot: String? = null

    @field:Column(name = "prazo_em", nullable = false)
    var prazoEm: OffsetDateTime? = null

    @field:Column(name = "hash_sha256", length = 64)
    var hashSha256: String? = null

    fun merge(solicitacao: Solicitacao) {
        this.tipoId = solicitacao.tipoId
        this.tipoCodigo = solicitacao.tipoCodigo
        this.tipoNome = solicitacao.tipoNome
        this.tipoVersao = solicitacao.tipoVersao
        this.solicitanteId = solicitacao.solicitanteId
        this.protocolo = solicitacao.protocolo.valor
        this.estado = solicitacao.estado
        this.payload = solicitacao.payloadJson
        this.formSchemaSnapshot = solicitacao.formSchemaSnapshot
        this.workflowSnapshot = solicitacao.workflowSnapshot
        this.prazoEm = solicitacao.prazoEm
        this.hashSha256 = solicitacao.hashSha256
    }

    fun toDomain(eventos: List<SolicitacaoEvento>): Solicitacao =
        Solicitacao(
            id!!,
            tipoId!!,
            tipoCodigo!!,
            tipoNome!!,
            tipoVersao,
            solicitanteId!!,
            Protocolo.of(protocolo),
            estado!!,
            payload,
            formSchemaSnapshot,
            workflowSnapshot,
            prazoEm,
            hashSha256,
            eventos,
            createdAt,
            updatedAt,
        )

    fun toDomainSemEventos(): Solicitacao = toDomain(emptyList())

    companion object {
        @JvmStatic
        fun fromDomain(solicitacao: Solicitacao): SolicitacaoJpaEntity {
            val entity = SolicitacaoJpaEntity()
            entity.id = solicitacao.id
            entity.merge(solicitacao)
            return entity
        }
    }
}
