package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SolicitacaoAnexoJpaRepository : JpaRepository<SolicitacaoAnexoJpaEntity, UUID> {
    fun findBySolicitacaoIdOrderByCreatedAtAsc(solicitacaoId: UUID): List<SolicitacaoAnexoJpaEntity>
}
