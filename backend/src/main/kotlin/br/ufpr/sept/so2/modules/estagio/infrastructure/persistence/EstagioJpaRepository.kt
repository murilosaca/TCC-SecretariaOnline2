package br.ufpr.sept.so2.modules.estagio.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
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

    fun existsByIdAluno(idAluno: UUID): Boolean
}
