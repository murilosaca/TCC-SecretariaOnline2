package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoAnexoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.SolicitacaoAnexo
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SolicitacaoAnexoJpaAdapter(
    private val jpaRepository: SolicitacaoAnexoJpaRepository,
) : SolicitacaoAnexoRepository {
    override fun save(anexo: SolicitacaoAnexo): SolicitacaoAnexo {
        jpaRepository.save(SolicitacaoAnexoJpaEntity.fromDomain(anexo))
        return anexo
    }

    override fun findBySolicitacaoId(solicitacaoId: UUID): List<SolicitacaoAnexo> =
        jpaRepository.findBySolicitacaoIdOrderByCreatedAtAsc(solicitacaoId).map { it.toDomain() }
}
