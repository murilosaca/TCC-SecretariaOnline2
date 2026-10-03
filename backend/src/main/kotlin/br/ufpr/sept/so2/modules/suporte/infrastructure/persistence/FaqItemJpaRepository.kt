package br.ufpr.sept.so2.modules.suporte.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FaqItemJpaRepository : JpaRepository<FaqItemJpaEntity, UUID> {
    fun findAllByOrderByOrdemAsc(): List<FaqItemJpaEntity>
}
