package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PeriodoLetivoJpaRepository extends JpaRepository<PeriodoLetivoJpaEntity, UUID> {

    boolean existsByAnoAndSemestre(short ano, short semestre);

    @Query("""
            SELECT p FROM PeriodoLetivoJpaEntity p
            WHERE p.ativo = true
              AND p.inicio <= :data
              AND p.fim >= :data
            """)
    Optional<PeriodoLetivoJpaEntity> findVigente(@Param("data") LocalDate data);
}
