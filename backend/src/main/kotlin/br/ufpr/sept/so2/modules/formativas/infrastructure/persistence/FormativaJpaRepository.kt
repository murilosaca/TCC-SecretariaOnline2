package br.ufpr.sept.so2.modules.formativas.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.UUID

interface FormativaJpaRepository : JpaRepository<FormativaJpaEntity, UUID> {
    fun findByIdEventoAndIdAluno(idEvento: UUID, idAluno: UUID): FormativaJpaEntity?

    fun findByIdAluno(idAluno: UUID, pageable: Pageable): Page<FormativaJpaEntity>

    fun findByEstado(estado: String, pageable: Pageable): Page<FormativaJpaEntity>

    fun findTop3ByIdAlunoAndEstadoOrderByCreatedAtDesc(idAluno: UUID, estado: String): List<FormativaJpaEntity>

    @Query(
        "select coalesce(sum(f.cargaHoraria), 0) from FormativaJpaEntity f " +
            "where f.idAluno = :idAluno and f.estado = :estado",
    )
    fun somarCargaHoraria(@Param("idAluno") idAluno: UUID, @Param("estado") estado: String): Long

    @Query(
        value = """
            SELECT f.* FROM formativa f
            INNER JOIN aluno a ON a.id = f.id_aluno
            WHERE a.id_curso IN (:cursoIds)
              AND f.estado = 'AGUARDANDO_CAAF'
              AND (f.id_responsavel IS NULL OR f.id_responsavel = :usuarioId)
            ORDER BY f.created_at ASC
            """,
        nativeQuery = true,
    )
    fun findPoolCaaf(
        @Param("cursoIds") cursoIds: Collection<UUID>,
        @Param("usuarioId") usuarioId: UUID,
    ): List<FormativaJpaEntity>

    @Query(
        value = """
            SELECT COUNT(*) FROM formativa f
            INNER JOIN aluno a ON a.id = f.id_aluno
            WHERE a.id_curso IN (:cursoIds)
              AND f.estado = 'AGUARDANDO_CAAF'
              AND f.id_responsavel IS NULL
            """,
        nativeQuery = true,
    )
    fun countSemResponsavel(@Param("cursoIds") cursoIds: Collection<UUID>): Long

    @Query(
        value = """
            SELECT COUNT(*) FROM formativa f
            INNER JOIN aluno a ON a.id = f.id_aluno
            WHERE a.id_curso IN (:cursoIds)
              AND f.estado = 'AGUARDANDO_CAAF'
              AND f.id_responsavel = :responsavelId
            """,
        nativeQuery = true,
    )
    fun countAtribuidasAguardando(
        @Param("cursoIds") cursoIds: Collection<UUID>,
        @Param("responsavelId") responsavelId: UUID,
    ): Long

    @Query(
        value = """
            SELECT COUNT(*) FROM formativa f
            INNER JOIN aluno a ON a.id = f.id_aluno
            WHERE a.id_curso IN (:cursoIds)
              AND f.estado = 'APROVADA'
              AND f.reviewed_at >= :inicio
            """,
        nativeQuery = true,
    )
    fun countAprovadasDesde(
        @Param("cursoIds") cursoIds: Collection<UUID>,
        @Param("inicio") inicio: OffsetDateTime,
    ): Long

    @Query(
        value = """
            SELECT CAST(f.id_responsavel AS varchar) AS idResponsavel, COUNT(*) AS total
            FROM formativa f
            INNER JOIN aluno a ON a.id = f.id_aluno
            WHERE a.id_curso IN (:cursoIds)
              AND f.estado = 'AGUARDANDO_CAAF'
              AND f.id_responsavel IN (:responsavelIds)
            GROUP BY f.id_responsavel
            """,
        nativeQuery = true,
    )
    fun countCargaAguardando(
        @Param("cursoIds") cursoIds: Collection<UUID>,
        @Param("responsavelIds") responsavelIds: Collection<UUID>,
    ): List<CargaResponsavel>

    interface CargaResponsavel {
        val idResponsavel: String
        val total: Long
    }
}
