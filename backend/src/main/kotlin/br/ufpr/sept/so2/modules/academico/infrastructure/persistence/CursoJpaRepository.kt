package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface CursoJpaRepository : JpaRepository<CursoJpaEntity, UUID> {
    fun existsBySiglaIgnoreCase(sigla: String): Boolean

    fun existsByCodigoIgnoreCase(codigo: String): Boolean

    fun findByCodigoIgnoreCase(codigo: String): Optional<CursoJpaEntity>
}
