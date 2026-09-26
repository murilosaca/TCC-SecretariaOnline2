package br.ufpr.sept.so2.modules.formativas.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ComissaoMembroJpaRepository : JpaRepository<ComissaoMembroJpaEntity, ComissaoMembroId> {
    fun findByIdUsuarioAndTipo(idUsuario: UUID, tipo: String): List<ComissaoMembroJpaEntity>

    fun findByIdCursoInAndTipo(cursoIds: Collection<UUID>, tipo: String): List<ComissaoMembroJpaEntity>

    fun existsByIdCursoAndIdUsuarioAndTipo(idCurso: UUID, idUsuario: UUID, tipo: String): Boolean
}
