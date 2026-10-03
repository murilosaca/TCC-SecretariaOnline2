package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.comunicacao.application.OutboxDispatcherMonitor.JobCard
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxConsultaPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxEvento
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ListarOutboxUseCase(
    private val outboxConsultaPort: OutboxConsultaPort,
    private val monitor: OutboxDispatcherMonitor,
) {
    fun execute(status: String?, tipo: String?, pageable: Pageable): OutboxPainel {
        val agora = OffsetDateTime.now()
        val maisAntigo = outboxConsultaPort.pendingMaisAntigo()
        val alerta = maisAntigo != null && Duration.between(maisAntigo, agora).seconds > LATENCIA_SEGUNDOS
        return OutboxPainel(
            outboxConsultaPort.listar(status.vazioComoNulo(), tipo.vazioComoNulo(), pageable),
            if (alerta) ALERTA else null,
            listOf(monitor.card()),
        )
    }

    private fun String?.vazioComoNulo(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    data class OutboxPainel(
        val pagina: Page<OutboxEvento>,
        val alertaLatencia: String?,
        val jobs: List<JobCard>,
    )

    companion object {
        const val LATENCIA_SEGUNDOS: Long = 30
        const val ALERTA = "Dispatcher com latência > 30s verificar OutboxDispatcher"
    }
}

@Service
class ReentregarOutboxUseCase(
    private val outboxConsultaPort: OutboxConsultaPort,
) {
    @Transactional
    fun execute(id: UUID, operadorId: UUID): OutboxEvento = outboxConsultaPort.reentregar(id, operadorId)
}
