package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class TipoSolicitacaoJpaAdapter(
    private val jpaRepository: TipoSolicitacaoJpaRepository,
) : TipoSolicitacaoRepository {

    override fun save(tipo: TipoSolicitacao): TipoSolicitacao {
        val entity = jpaRepository.findById(tipo.id)
            .orElseGet { TipoSolicitacaoJpaEntity.fromDomain(tipo) }
        entity.merge(tipo)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Optional<TipoSolicitacao> =
        jpaRepository.findById(id).map(TipoSolicitacaoJpaEntity::toDomain)

    override fun findByCodigo(codigo: String): Optional<TipoSolicitacao> =
        jpaRepository.findByCodigoIgnoreCase(codigo).map(TipoSolicitacaoJpaEntity::toDomain)

    override fun findPublished(pageable: Pageable): Page<TipoSolicitacao> =
        jpaRepository.findByStatus(TipoSolicitacao.PUBLISHED, pageable)
            .map(TipoSolicitacaoJpaEntity::toDomain)
}
