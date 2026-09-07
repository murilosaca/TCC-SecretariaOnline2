package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

interface PeriodoLetivoJpaRepository : JpaRepository<PeriodoLetivoJpaEntity, UUID> {
    fun existsByAnoAndSemestre(ano: Short, semestre: Short): Boolean

    @Query(
        """
        SELECT p FROM PeriodoLetivoJpaEntity p
        WHERE p.ativo = true
          AND p.inicio <= :data
          AND p.fim >= :data
        """,
    )
    fun findVigente(@Param("data") data: LocalDate): Optional<PeriodoLetivoJpaEntity>
}
