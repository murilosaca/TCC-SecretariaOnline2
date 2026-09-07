package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

interface EventoJpaRepository : JpaRepository<EventoJpaEntity, UUID> {
    fun existsByTituloIgnoreCase(titulo: String): Boolean

    fun findByTituloIgnoreCase(titulo: String): Optional<EventoJpaEntity>

    fun findByIdAnfitriao(idAnfitriao: UUID, pageable: Pageable): Page<EventoJpaEntity>

    @Query(
        """
            SELECT e FROM EventoJpaEntity e
            WHERE e.estado = 'EM_ANDAMENTO'
              AND e.janelaEntradaInicio IS NOT NULL
              AND e.janelaEntradaFim IS NOT NULL
              AND e.janelaEntradaInicio <= :agora
              AND e.janelaEntradaFim >= :agora
            """,
    )
    fun findAbertosParaAluno(@Param("agora") agora: OffsetDateTime, pageable: Pageable): Page<EventoJpaEntity>

    @Query(
        """
            SELECT e FROM EventoJpaEntity e
            WHERE e.estado = 'EM_ANDAMENTO'
              AND e.janelaEntradaInicio IS NOT NULL
              AND e.janelaEntradaFim IS NOT NULL
              AND e.janelaEntradaInicio < :fimDia
              AND e.janelaEntradaFim >= :inicioDia
            ORDER BY e.inicioEm ASC
            """,
    )
    fun findEmAndamentoComJanelaNoDia(
        @Param("inicioDia") inicioDia: OffsetDateTime,
        @Param("fimDia") fimDia: OffsetDateTime,
    ): List<EventoJpaEntity>
}
