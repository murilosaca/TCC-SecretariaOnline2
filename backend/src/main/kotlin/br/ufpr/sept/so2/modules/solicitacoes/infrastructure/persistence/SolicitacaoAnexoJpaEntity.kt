package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import br.ufpr.sept.so2.modules.solicitacoes.domain.SolicitacaoAnexo
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "solicitacao_anexo")
class SolicitacaoAnexoJpaEntity {
    @Id
    @Column(nullable = false)
    var id: UUID? = null

    @Column(name = "solicitacao_id", nullable = false)
    var solicitacaoId: UUID? = null

    @Column(nullable = false, length = 255)
    var nome: String? = null

    @Column(name = "content_type", nullable = false, length = 100)
    var contentType: String? = null

    @Column(nullable = false)
    var tamanho: Int = 0

    @Column(nullable = false, length = 64)
    var sha256: String? = null

    @Column(name = "storage_key", nullable = false, length = 500)
    var storageKey: String? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime? = null

    fun toDomain(): SolicitacaoAnexo =
        SolicitacaoAnexo(
            id!!,
            solicitacaoId!!,
            nome!!,
            contentType!!,
            tamanho,
            sha256!!,
            storageKey!!,
            createdAt!!,
        )

    companion object {
        fun fromDomain(anexo: SolicitacaoAnexo): SolicitacaoAnexoJpaEntity {
            val entity = SolicitacaoAnexoJpaEntity()
            entity.id = anexo.id
            entity.solicitacaoId = anexo.solicitacaoId
            entity.nome = anexo.nome
            entity.contentType = anexo.contentType
            entity.tamanho = anexo.tamanho
            entity.sha256 = anexo.sha256
            entity.storageKey = anexo.storageKey
            entity.createdAt = anexo.createdAt
            return entity
        }
    }
}
