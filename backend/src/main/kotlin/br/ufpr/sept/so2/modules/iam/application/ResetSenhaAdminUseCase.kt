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
 * F7.8: dispara JWT de uso único + Outbox. O operador nunca vê o link nem a senha.
 * O consumo é em /nova-senha (API /auth/redefinir-senha).
 *
 * F5.6-D04: o diagrama pede senha temporária em texto no e-mail. RF-F7-001 e esta
 * base proíbem senha em claro no audit, no log e na resposta HTTP. O reset da
 * secretaria reusa este JWT de uso único. O outbox é PASSWORD_RESET_BY_ADMIN,
 * distinto de PASSWORD_RESET (esqueci-minha-senha). O audit é user.password_reset,
 * com operador e alvo, sem token e sem senha. A rota acadêmica exige só
 * user.manage_students; não concede user.reset_password.
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
        disparar(operadorId, targetUserId, ip, "PASSWORD_RESET", "iam.password_reset_admin")
    }

    @Transactional
    fun executeParaSecretaria(operadorId: UUID, targetUserId: UUID, ip: String?) {
        disparar(operadorId, targetUserId, ip, "PASSWORD_RESET_BY_ADMIN", "user.password_reset")
    }

    private fun disparar(
        operadorId: UUID,
        targetUserId: UUID,
        ip: String?,
        tipoOutbox: String,
        tipoAudit: String,
    ) {
        val usuario = usuarioRepository.findById(targetUserId)
            .orElseThrow { RecursoNaoEncontradoException("Usuário não encontrado.") }
        if (!usuario.ativo) {
            throw ConflitoEstadoException("Não é possível resetar senha de usuário inativo.")
        }
        val token = jwtTokenService.emitResetToken(usuario)
        val url = settings.frontendBaseUrl.trimEnd('/') + "/nova-senha?token=" + token
        outboxPort.enqueue(
            tipoOutbox,
            toJson(
                mapOf(
                    "usuarioId" to usuario.id.toString(),
                    "resetUrl" to url,
                ),
            ),
        )
        auditLogPort.append(
            tipoAudit,
            operadorId,
            """{"operadorId":"$operadorId","targetUserId":"$targetUserId"}""",
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
