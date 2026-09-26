package br.ufpr.sept.so2.modules.tcc.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface TccJpaRepository : JpaRepository<TccJpaEntity, UUID> {
    fun findByIdAluno(idAluno: UUID, pageable: Pageable): Page<TccJpaEntity>

    fun findByIdAlunoAndEstado(idAluno: UUID, estado: String, pageable: Pageable): Page<TccJpaEntity>

    fun findByIdCursoIn(idCursos: Collection<UUID>, pageable: Pageable): Page<TccJpaEntity>

    fun findByIdCursoInAndSituacao(
        idCursos: Collection<UUID>,
        situacao: String,
        pageable: Pageable,
    ): Page<TccJpaEntity>

    fun existsByIdAlunoAndSituacao(idAluno: UUID, situacao: String): Boolean

    @Query(
        """
        select t from TccJpaEntity t join t.membros m
        where m.idUsuario = :usuarioId and t.estado = :estado
        """,
        countQuery = """
        select count(t) from TccJpaEntity t join t.membros m
        where m.idUsuario = :usuarioId and t.estado = :estado
        """,
    )
    fun findParaRevisao(
        @Param("usuarioId") usuarioId: UUID,
        @Param("estado") estado: String,
        pageable: Pageable,
    ): Page<TccJpaEntity>
}
