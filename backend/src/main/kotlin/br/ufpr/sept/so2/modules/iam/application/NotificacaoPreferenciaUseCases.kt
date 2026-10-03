package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.NotificacaoPreferenciaRepository
import br.ufpr.sept.so2.modules.iam.domain.CanalNotificacao
import br.ufpr.sept.so2.modules.iam.domain.ModoDigest
import br.ufpr.sept.so2.modules.iam.domain.NotificacaoPreferencia
import br.ufpr.sept.so2.modules.iam.domain.NotificacaoPreferenciaRegras
import br.ufpr.sept.so2.modules.iam.domain.PrioridadeNotificacao
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

data class NotificacaoPreferenciaPatch(
    val canais: Map<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>>?,
    val dndInicio: CampoPatch<LocalTime?>,
    val dndFim: CampoPatch<LocalTime?>,
    val digest: ModoDigest?,
)

@Service
class ConsultarNotificacaoPreferenciaUseCase(
    private val repository: NotificacaoPreferenciaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID): NotificacaoPreferencia =
        repository.findByUsuarioId(usuarioId)
            .orElseGet { NotificacaoPreferenciaRegras.padrao(usuarioId, OffsetDateTime.now()) }
}

@Service
class AtualizarNotificacaoPreferenciaUseCase(
    private val repository: NotificacaoPreferenciaRepository,
    private val auditLogPort: AuditLogPort,
) {
    @Transactional
    fun execute(usuarioId: UUID, patch: NotificacaoPreferenciaPatch, ip: String?): NotificacaoPreferencia {
        val agora = OffsetDateTime.now()
        val atual = repository.findByUsuarioId(usuarioId)
            .orElseGet { NotificacaoPreferenciaRegras.padrao(usuarioId, agora) }
        val salva = NotificacaoPreferenciaRegras.aplicar(
            atual,
            patch.canais,
            patch.dndInicio.presente,
            patch.dndInicio.valor,
            patch.dndFim.presente,
            patch.dndFim.valor,
            patch.digest,
            agora,
        )
        val persistida = repository.save(salva)
        auditLogPort.append("iam.notification_prefs_updated", usuarioId, "ok", ip)
        return persistida
    }
}
