package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SolicitacaoEventoJpaRepository : JpaRepository<SolicitacaoEventoJpaEntity, UUID> {

    fun findBySolicitacaoIdOrderByCreatedAtDesc(solicitacaoId: UUID): List<SolicitacaoEventoJpaEntity>
}
