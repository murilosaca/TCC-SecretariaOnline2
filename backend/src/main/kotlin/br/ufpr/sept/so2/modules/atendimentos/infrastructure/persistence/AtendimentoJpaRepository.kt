package br.ufpr.sept.so2.modules.atendimentos.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AtendimentoJpaRepository : JpaRepository<AtendimentoJpaEntity, UUID> {
    @Query(
        """
        SELECT a FROM AtendimentoJpaEntity a
        WHERE a.idAluno = :alunoId
          AND (:estado IS NULL OR a.estado = :estado)
        """,
    )
    fun findByAluno(
        @Param("alunoId") alunoId: UUID,
        @Param("estado") estado: String?,
        pageable: Pageable,
    ): Page<AtendimentoJpaEntity>
}
