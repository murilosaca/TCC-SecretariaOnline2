package br.ufpr.sept.so2.modules.comunicacao.infrastructure

import br.ufpr.sept.so2.modules.comunicacao.application.DespacharOutboxUseCase
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.comunicacao.dispatcher-enabled"], havingValue = "true", matchIfMissing = true)
class OutboxDispatcherJob(
    private val despacharOutboxUseCase: DespacharOutboxUseCase,
) {
    @Scheduled(fixedDelay = 5000)
    fun tick() {
        despacharOutboxUseCase.execute()
    }
}
