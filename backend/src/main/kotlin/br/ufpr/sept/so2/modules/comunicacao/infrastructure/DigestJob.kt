package br.ufpr.sept.so2.modules.comunicacao.infrastructure

import br.ufpr.sept.so2.modules.comunicacao.application.EnfileirarDigestUseCase
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["app.comunicacao.dispatcher-enabled"], havingValue = "true", matchIfMissing = true)
class DigestJob(
    private val enfileirarDigestUseCase: EnfileirarDigestUseCase,
) {
    @Scheduled(fixedDelay = 60_000)
    fun tick() {
        try {
            val enviados = enfileirarDigestUseCase.execute()
            if (enviados > 0) {
                LOG.info("Digest enfileirado para {} destinatário(s).", enviados)
            }
        } catch (ex: Exception) {
            LOG.error("Digest não enfileirado: {}", ex.message)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DigestJob::class.java)
    }
}
