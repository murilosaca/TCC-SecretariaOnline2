package br.ufpr.sept.so2.modules.estagio.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.UUID

interface EstagioJpaRepository : JpaRepository<EstagioJpaEntity, UUID> {
    fun findByIdAluno(idAluno: UUID, pageable: Pageable): Page<EstagioJpaEntity>

    fun findByIdAlunoAndSituacao(idAluno: UUID, situacao: String, pageable: Pageable): Page<EstagioJpaEntity>

    fun findByIdOrientadorAndSituacaoIn(
        idOrientador: UUID,
        situacoes: Collection<String>,
        pageable: Pageable,
    ): Page<EstagioJpaEntity>

    fun findByIdOrientadorAndSituacao(
        idOrientador: UUID,
        situacao: String,
        pageable: Pageable,
    ): Page<EstagioJpaEntity>

    fun findByIdCursoIn(idCursos: Collection<UUID>, pageable: Pageable): Page<EstagioJpaEntity>

    fun findByIdCursoInAndSituacao(
        idCursos: Collection<UUID>,
        situacao: String,
        pageable: Pageable,
    ): Page<EstagioJpaEntity>

    fun existsByIdAluno(idAluno: UUID): Boolean

    fun existsByIdCursoAndIdOrientadorIsNullAndSituacaoNot(idCurso: UUID, situacao: String): Boolean

    @Query(
        """
        SELECT e FROM EstagioJpaEntity e
        WHERE e.idCurso IN :cursoIds
          AND e.situacao <> 'CONCLUIDO'
          AND (e.idOrientador IS NULL OR e.idOrientador = :usuarioId)
        ORDER BY e.inicio ASC, e.createdAt ASC
        """,
    )
    fun findPoolCoe(
        @Param("cursoIds") cursoIds: Collection<UUID>,
        @Param("usuarioId") usuarioId: UUID,
    ): List<EstagioJpaEntity>

    @Query(
        """
        SELECT COUNT(e) FROM EstagioJpaEntity e
        WHERE e.idCurso IN :cursoIds
          AND e.situacao <> 'CONCLUIDO'
          AND e.idOrientador IS NULL
        """,
    )
    fun countSemOrientador(@Param("cursoIds") cursoIds: Collection<UUID>): Long

    @Query(
        """
        SELECT COUNT(e) FROM EstagioJpaEntity e
        WHERE e.idCurso IN :cursoIds
          AND e.situacao <> 'CONCLUIDO'
          AND e.idOrientador = :orientadorId
        """,
    )
    fun countAtribuidosAtivos(
        @Param("cursoIds") cursoIds: Collection<UUID>,
        @Param("orientadorId") orientadorId: UUID,
    ): Long

    @Query(
        """
        SELECT COUNT(e) FROM EstagioJpaEntity e
        WHERE e.idCurso IN :cursoIds
          AND e.situacao = 'CONCLUIDO'
          AND e.updatedAt >= :inicio
        """,
    )
    fun countConcluidosDesde(
        @Param("cursoIds") cursoIds: Collection<UUID>,
        @Param("inicio") inicio: OffsetDateTime,
    ): Long

    @Query(
        """
        SELECT e.idOrientador AS idOrientador, COUNT(e) AS total FROM EstagioJpaEntity e
        WHERE e.idCurso IN :cursoIds
          AND e.situacao <> 'CONCLUIDO'
          AND e.idOrientador IN :orientadorIds
        GROUP BY e.idOrientador
        """,
    )
    fun countCargaAtiva(
        @Param("cursoIds") cursoIds: Collection<UUID>,
        @Param("orientadorIds") orientadorIds: Collection<UUID>,
    ): List<CargaOrientador>

    interface CargaOrientador {
        val idOrientador: UUID
        val total: Long
    }
}
