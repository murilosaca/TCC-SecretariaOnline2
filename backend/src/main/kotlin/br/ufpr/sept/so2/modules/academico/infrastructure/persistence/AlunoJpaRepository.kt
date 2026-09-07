package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.Optional
import java.util.UUID

interface AlunoJpaRepository : JpaRepository<AlunoJpaEntity, UUID>, JpaSpecificationExecutor<AlunoJpaEntity> {
    fun findByGrrIgnoreCase(grr: String): Optional<AlunoJpaEntity>

    fun findByEmailInstitucionalIgnoreCase(email: String): Optional<AlunoJpaEntity>

    fun existsByGrrIgnoreCase(grr: String): Boolean

    fun existsByEmailInstitucionalIgnoreCase(email: String): Boolean

    fun findByIdCurso(idCurso: UUID, pageable: Pageable): Page<AlunoJpaEntity>
}
