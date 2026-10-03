package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

data class SessaoDispositivoVisao(
    val id: UUID,
    val dispositivo: String,
    val atual: Boolean,
    val criadaEm: OffsetDateTime,
    val expiraEm: OffsetDateTime,
    val encerrar: Boolean,
)

@Service
class ListarSessoesUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Transactional(readOnly = true)
    fun execute(usuarioId: UUID, sessaoAtualId: UUID?): List<SessaoDispositivoVisao> {
        val agora = OffsetDateTime.now()
        return refreshTokenRepository.findAtivas(usuarioId, agora).map { sessao ->
            val atual = sessaoAtualId != null && sessao.id == sessaoAtualId
            SessaoDispositivoVisao(
                sessao.id,
                DispositivoLabel.de(sessao.userAgent),
                atual,
                sessao.createdAt,
                sessao.expiresAt,
                !atual,
            )
        }
    }
}

@Service
class EncerrarSessaoUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val auditLogPort: AuditLogPort,
) {
    @Transactional
    fun execute(usuarioId: UUID, sessaoId: UUID, sessaoAtualId: UUID?, ip: String?) {
        if (sessaoAtualId != null && sessaoId == sessaoAtualId) {
            throw ConflitoEstadoException("A sessão atual não pode ser encerrada por este recurso.")
        }
        val agora = OffsetDateTime.now()
        val sessao = refreshTokenRepository.findById(sessaoId)
            .orElseThrow { RecursoNaoEncontradoException("Sessão não encontrada.") }
        if (sessao.usuarioId != usuarioId || sessao.revoked || sessao.used || sessao.expirada(agora)) {
            throw RecursoNaoEncontradoException("Sessão não encontrada.")
        }
        sessao.revogar(agora)
        refreshTokenRepository.save(sessao)
        auditLogPort.append("iam.session_revoked", usuarioId, "ok", ip)
    }
}
