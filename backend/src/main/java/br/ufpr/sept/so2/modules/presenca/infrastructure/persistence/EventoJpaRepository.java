package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoJpaRepository extends JpaRepository<EventoJpaEntity, UUID> {

    boolean existsByTituloIgnoreCase(String titulo);

    Optional<EventoJpaEntity> findByTituloIgnoreCase(String titulo);

    Page<EventoJpaEntity> findByIdAnfitriao(UUID idAnfitriao, Pageable pageable);

    @Query("""
            SELECT e FROM EventoJpaEntity e
            WHERE e.estado = 'EM_ANDAMENTO'
              AND e.janelaEntradaInicio IS NOT NULL
              AND e.janelaEntradaFim IS NOT NULL
              AND e.janelaEntradaInicio <= :agora
              AND e.janelaEntradaFim >= :agora
            """)
    Page<EventoJpaEntity> findAbertosParaAluno(@Param("agora") OffsetDateTime agora, Pageable pageable);

    @Query("""
            SELECT e FROM EventoJpaEntity e
            WHERE e.estado = 'EM_ANDAMENTO'
              AND e.janelaEntradaInicio IS NOT NULL
              AND e.janelaEntradaFim IS NOT NULL
              AND e.janelaEntradaInicio < :fimDia
              AND e.janelaEntradaFim >= :inicioDia
            ORDER BY e.inicioEm ASC
            """)
    List<EventoJpaEntity> findEmAndamentoComJanelaNoDia(
            @Param("inicioDia") OffsetDateTime inicioDia,
            @Param("fimDia") OffsetDateTime fimDia
    );
}
