package br.ufpr.sept.so2.modules.atendimentos.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface CategoriaAtendimentoJpaRepository : JpaRepository<CategoriaAtendimentoJpaEntity, UUID> {
    fun findByNomeIgnoreCase(nome: String): Optional<CategoriaAtendimentoJpaEntity>

    fun findByAtivoTrueOrderByNomeAsc(): List<CategoriaAtendimentoJpaEntity>
}
