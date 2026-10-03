package br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence

import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoEntregaRepository
import br.ufpr.sept.so2.modules.comunicacao.domain.ComunicacaoEntrega
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ComunicacaoEntregaJpaAdapter(
    private val jpaRepository: ComunicacaoEntregaJpaRepository,
) : ComunicacaoEntregaRepository {
    override fun save(entrega: ComunicacaoEntrega): ComunicacaoEntrega {
        val entity = jpaRepository.findById(entrega.id).orElseGet { ComunicacaoEntregaJpaEntity.fromDomain(entrega) }
        entity.comunicacaoId = entrega.comunicacaoId
        entity.destinatarioId = entrega.destinatarioId
        entity.readAt = entrega.readAt
        entity.acaoHref = entrega.acaoHref
        entity.inApp = entrega.inApp
        entity.emailEnviado = entrega.emailEnviado
        return jpaRepository.save(entity).toDomain()
    }

    override fun findVisiveis(destinatarioId: UUID): List<ComunicacaoEntrega> =
        jpaRepository.findByDestinatarioIdAndInAppTrue(destinatarioId).map { it.toDomain() }

    override fun findByComunicacaoEDestinatario(comunicacaoId: UUID, destinatarioId: UUID): ComunicacaoEntrega? =
        jpaRepository.findByComunicacaoIdAndDestinatarioId(comunicacaoId, destinatarioId)
            .map { it.toDomain() }
            .orElse(null)
}
