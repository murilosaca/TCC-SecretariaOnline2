package br.ufpr.sept.so2.modules.diplomas.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface DiplomaJpaRepository : JpaRepository<DiplomaJpaEntity, UUID> {
    fun findByIdAluno(idAluno: UUID): Optional<DiplomaJpaEntity>

    fun existsByIdAluno(idAluno: UUID): Boolean

    @Query(
        """
        SELECT d FROM DiplomaJpaEntity d
        WHERE d.idCurso = :cursoId
          AND (:situacao IS NULL OR d.situacao = :situacao)
        """,
    )
    fun findByCurso(
        @Param("cursoId") cursoId: UUID,
        @Param("situacao") situacao: String?,
        pageable: Pageable,
    ): Page<DiplomaJpaEntity>
}
