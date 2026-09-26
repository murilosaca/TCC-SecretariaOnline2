package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * F7.8: dispara o mesmo JWT 1-uso + Outbox PASSWORD_RESET do fluxo público.
 * O operador nunca vê o link nem a senha; o consumo é em /nova-senha (API /auth/redefinir-senha).
 */
@Service
class ResetSenhaAdminUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val jwtTokenService: JwtTokenService,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val settings: IamSettings,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(operadorId: UUID, targetUserId: UUID, ip: String?) {
        val usuario = usuarioRepository.findById(targetUserId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        if (!usuario.ativo) {
            throw ConflitoEstadoException("Não é possível resetar senha de usuário inativo.")
        }
        val token = jwtTokenService.emitResetToken(usuario)
        val url = settings.frontendBaseUrl.trimEnd('/') + "/nova-senha?token=" + token
        val payload = toJson(
            mapOf(
                "usuarioId" to usuario.id.toString(),
                "resetUrl" to url,
            ),
        )
        outboxPort.enqueue("PASSWORD_RESET", payload)
        auditLogPort.append(
            "iam.password_reset_admin",
            operadorId,
            """{"targetUserId":"$targetUserId","acao":"RESET_PASSWORD"}""",
            ip,
        )
    }

    private fun toJson(valor: Map<String, String>): String {
        try {
            return objectMapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível enfileirar o reset de senha.")
        }
    }
}
