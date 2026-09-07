package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import br.ufpr.sept.so2.modules.solicitacoes.domain.SolicitacaoEvento
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "solicitacao_evento")
class SolicitacaoEventoJpaEntity protected constructor() {

    @field:Id
    @field:Column(columnDefinition = "uuid", nullable = false, updatable = false)
    var id: UUID? = null

    @field:Column(name = "solicitacao_id", nullable = false)
    var solicitacaoId: UUID? = null

    @field:Column(nullable = false, length = 80)
    var tipo: String? = null

    @field:Column(name = "estado_de", length = 40)
    var estadoDe: String? = null

    @field:Column(name = "estado_para", nullable = false, length = 40)
    var estadoPara: String? = null

    @field:Column(name = "ator_id")
    var atorId: UUID? = null

    @field:Column(columnDefinition = "text")
    var parecer: String? = null

    @field:Column(columnDefinition = "text")
    var payload: String? = null

    @field:Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null

    fun toDomain(): SolicitacaoEvento =
        SolicitacaoEvento(id!!, tipo!!, estadoDe, estadoPara!!, atorId, parecer, payload, createdAt!!)

    companion object {
        @JvmStatic
        fun fromDomain(solicitacaoId: UUID, evento: SolicitacaoEvento): SolicitacaoEventoJpaEntity {
            val entity = SolicitacaoEventoJpaEntity()
            entity.id = evento.id
            entity.solicitacaoId = solicitacaoId
            entity.tipo = evento.tipo
            entity.estadoDe = evento.estadoDe
            entity.estadoPara = evento.estadoPara
            entity.atorId = evento.atorId
            entity.parecer = evento.parecer
            entity.payload = evento.payload
            entity.createdAt = evento.createdAt
            return entity
        }
    }
}
