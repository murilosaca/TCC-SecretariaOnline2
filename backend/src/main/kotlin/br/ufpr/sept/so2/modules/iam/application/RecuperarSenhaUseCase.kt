package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecuperarSenhaUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val jwtTokenService: JwtTokenService,
    private val outboxPort: OutboxPort,
    private val auditLogPort: AuditLogPort,
    private val settings: IamSettings,
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
        val url = settings.frontendBaseUrl + "/nova-senha?token=" + token
        val payload = "{\"email\":\"$mascarado\",\"resetUrl\":\"$url\"}"
        outboxPort.enqueue("PASSWORD_RESET", payload)
        auditLogPort.append("iam.password_reset_requested", encontrado.id, mascarado, ip)
    }
}
