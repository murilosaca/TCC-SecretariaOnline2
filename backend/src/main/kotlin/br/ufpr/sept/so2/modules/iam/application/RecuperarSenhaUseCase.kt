package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecuperarSenhaUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val jwtTokenService: JwtTokenService,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val settings: IamSettings,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun execute(email: String?, ip: String?) {
        val normalizado = Email.of(email).value
        val mascarado = IdentificadorLogin.tryParse(normalizado)?.mascarado() ?: "***"
        val encontrado = usuarioRepository.findByEmail(normalizado)
            .filter { it.ativo }
            .orElse(null)
        if (encontrado == null) {
            auditLogPort.append("iam.password_reset_requested", null, mascarado, ip)
            return
        }
        val token = jwtTokenService.emitResetToken(encontrado)
        val url = settings.frontendBaseUrl.trimEnd('/') + "/nova-senha?token=" + token
        val payload = toJson(
            mapOf(
                "usuarioId" to encontrado.id.toString(),
                "resetUrl" to url,
            ),
        )
        outboxPort.enqueue("PASSWORD_RESET", payload)
        auditLogPort.append("iam.password_reset_requested", encontrado.id, mascarado, ip)
    }

    private fun toJson(valor: Map<String, String>): String {
        try {
            return objectMapper.writeValueAsString(valor)
        } catch (_: JsonProcessingException) {
            throw DadoInvalidoException("Não foi possível enfileirar a recuperação de senha.")
        }
    }
}
