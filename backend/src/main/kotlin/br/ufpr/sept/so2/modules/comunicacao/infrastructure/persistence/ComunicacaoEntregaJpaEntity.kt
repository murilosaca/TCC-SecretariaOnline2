package br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence

import br.ufpr.sept.so2.modules.comunicacao.domain.ComunicacaoEntrega
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "comunicacao_entrega",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_comunicacao_destinatario", columnNames = ["id_comunicacao", "id_destinatario"]),
    ],
)
class ComunicacaoEntregaJpaEntity : BaseEntity() {
    @Column(name = "id_comunicacao", nullable = false)
    var comunicacaoId: UUID? = null

    @Column(name = "id_destinatario", nullable = false)
    var destinatarioId: UUID? = null

    @Column(name = "read_at")
    var readAt: OffsetDateTime? = null

    @Column(name = "acao_href", length = 300)
    var acaoHref: String? = null

    @Column(name = "in_app", nullable = false)
    var inApp: Boolean = true

    @Column(name = "email_enviado", nullable = false)
    var emailEnviado: Boolean = false

    fun toDomain(): ComunicacaoEntrega = ComunicacaoEntrega(
        requireNotNull(id),
        requireNotNull(comunicacaoId),
        requireNotNull(destinatarioId),
        readAt,
        acaoHref,
        inApp,
        emailEnviado,
        requireNotNull(createdAt),
    )

    companion object {
        fun fromDomain(entrega: ComunicacaoEntrega) = ComunicacaoEntregaJpaEntity().apply {
            id = entrega.id
            comunicacaoId = entrega.comunicacaoId
            destinatarioId = entrega.destinatarioId
            readAt = entrega.readAt
            acaoHref = entrega.acaoHref
            inApp = entrega.inApp
            emailEnviado = entrega.emailEnviado
        }
    }
}
