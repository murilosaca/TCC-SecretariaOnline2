package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import org.springframework.stereotype.Component

@Component
class OutboxJpaAdapter(
    private val jpaRepository: OutboxEventJpaRepository,
) : OutboxPort {
    override fun enqueue(tipo: String, payload: String) {
        jpaRepository.save(OutboxEventJpaEntity(tipo, payload))
    }
}
