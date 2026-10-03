package br.ufpr.sept.so2.modules.comunicacao.infrastructure

import br.ufpr.sept.so2.modules.comunicacao.application.DespacharOutboxUseCase
import br.ufpr.sept.so2.modules.comunicacao.application.OutboxDispatcherMonitor
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.comunicacao.dispatcher-enabled"], havingValue = "true", matchIfMissing = true)
class OutboxDispatcherJob(
    private val despacharOutboxUseCase: DespacharOutboxUseCase,
    private val monitor: OutboxDispatcherMonitor,
) {
    @Scheduled(fixedDelay = 5000)
    fun tick() {
        try {
            despacharOutboxUseCase.execute()
            monitor.registrarOk()
        } catch (ex: Exception) {
            monitor.registrarFalha(ex.message)
            LOG.error("OutboxDispatcher falhou: {}", ex.message)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(OutboxDispatcherJob::class.java)
    }
}
