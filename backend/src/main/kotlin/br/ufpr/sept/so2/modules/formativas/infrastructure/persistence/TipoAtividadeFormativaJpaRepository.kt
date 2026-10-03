package br.ufpr.sept.so2.modules.formativas.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface TipoAtividadeFormativaJpaRepository : JpaRepository<TipoAtividadeFormativaJpaEntity, UUID> {
    fun findByCursoIdAndAtivoTrueOrderByNomeAsc(cursoId: UUID): List<TipoAtividadeFormativaJpaEntity>

    fun findByCursoIdAndNome(cursoId: UUID, nome: String): Optional<TipoAtividadeFormativaJpaEntity>
}
