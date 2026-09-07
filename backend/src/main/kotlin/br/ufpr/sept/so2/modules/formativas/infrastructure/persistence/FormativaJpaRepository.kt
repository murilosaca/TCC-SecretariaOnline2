package br.ufpr.sept.so2.modules.formativas.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface FormativaJpaRepository : JpaRepository<FormativaJpaEntity, UUID> {
    fun findByIdEventoAndIdAluno(idEvento: UUID, idAluno: UUID): FormativaJpaEntity?

    fun findByIdAluno(idAluno: UUID, pageable: Pageable): Page<FormativaJpaEntity>

    fun findTop3ByIdAlunoAndEstadoOrderByCreatedAtDesc(idAluno: UUID, estado: String): List<FormativaJpaEntity>

    @Query(
        "select coalesce(sum(f.cargaHoraria), 0) from FormativaJpaEntity f " +
            "where f.idAluno = :idAluno and f.estado = :estado",
    )
    fun somarCargaHoraria(@Param("idAluno") idAluno: UUID, @Param("estado") estado: String): Long
}
