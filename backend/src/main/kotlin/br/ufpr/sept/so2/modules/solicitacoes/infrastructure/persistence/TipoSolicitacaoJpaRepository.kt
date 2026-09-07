package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface TipoSolicitacaoJpaRepository : JpaRepository<TipoSolicitacaoJpaEntity, UUID> {

    fun findByCodigoIgnoreCase(codigo: String): Optional<TipoSolicitacaoJpaEntity>

    fun findByStatus(status: String, pageable: Pageable): Page<TipoSolicitacaoJpaEntity>
}
