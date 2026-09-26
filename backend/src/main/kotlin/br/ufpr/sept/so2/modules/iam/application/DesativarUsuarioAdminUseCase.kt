package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class DesativarUsuarioAdminUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val auditLogPort: AuditLogPort,
) {
    @Transactional
    fun execute(operadorId: UUID, id: UUID, ip: String?): Usuario {
        val usuario = usuarioRepository.findById(id)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        val agora = OffsetDateTime.now()
        usuario.desativar(agora)
        val persistido = usuarioRepository.save(usuario)
        refreshTokenRepository.revokeAllByUsuarioId(id)
        auditLogPort.append(
            "iam.user_deactivated",
            operadorId,
            """{"targetUserId":"$id"}""",
            ip,
        )
        return persistido
    }
}
