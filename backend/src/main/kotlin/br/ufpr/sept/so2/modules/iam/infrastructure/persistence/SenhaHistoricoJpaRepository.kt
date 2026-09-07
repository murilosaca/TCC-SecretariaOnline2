package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SenhaHistoricoJpaRepository : JpaRepository<SenhaHistoricoJpaEntity, UUID> {
    fun findTop3ByUsuarioIdOrderByCreatedAtDesc(usuarioId: UUID): List<SenhaHistoricoJpaEntity>
}
