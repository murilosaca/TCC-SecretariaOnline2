package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.modules.iam.application.ports.OpaqueTokenHasher
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin
import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.exception.CredenciaisInvalidasException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class LoginUseCase(
    private val usuarioRepository: UsuarioRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtTokenService: JwtTokenService,
    private val opaqueTokenHasher: OpaqueTokenHasher,
    private val auditLogPort: AuditLogPort,
    private val settings: IamSettings,
) {
    @Transactional
    fun execute(identificador: String?, senha: String?, ip: String?): LoginResult {
        val parsed = IdentificadorLogin.tryParse(identificador)
        val encontrado = parsed?.let { usuarioRepository.findByIdentificador(it) }?.orElse(null)
        if (encontrado == null) {
            recusar(null, parsed?.mascarado() ?: "***", ip)
        }

        val agora = OffsetDateTime.now()
        encontrado.liberarBloqueioSeExpirado(agora)
        if (!encontrado.ativo || encontrado.estaBloqueado(agora)) {
            recusar(encontrado.id, mascarar(encontrado), ip)
        }
        if (!passwordHasher.matches(senha, encontrado.senhaHash)) {
            val bloqueou = encontrado.registrarFalha(
                agora,
                settings.maxFalhasConsecutivas,
                settings.minutosBloqueio,
            )
            usuarioRepository.save(encontrado)
            if (bloqueou) {
                auditLogPort.append("iam.account_blocked", encontrado.id, mascarar(encontrado), ip)
            }
            recusar(encontrado.id, mascarar(encontrado), ip)
        }

        encontrado.registrarLoginOk(agora)
        usuarioRepository.save(encontrado)
        val refreshRaw = UUID.randomUUID().toString()
        val sessao = RefreshSessao(
            Uuids.v7(),
            encontrado.id,
            opaqueTokenHasher.hash(refreshRaw),
            agora.plusSeconds(settings.refreshTtlSeconds),
            false,
            false,
            agora,
            agora,
        )
        refreshTokenRepository.save(sessao)
        auditLogPort.append("iam.login_success", encontrado.id, mascarar(encontrado), ip)
        return LoginResult(
            jwtTokenService.emitAccessToken(encontrado),
            refreshRaw,
            encontrado.precisaPrimeiroAcesso(),
            settings.accessTtlSeconds,
        )
    }

    private fun recusar(atorId: UUID?, payload: String, ip: String?): Nothing {
        passwordHasher.matchesDummy()
        auditLogPort.append("iam.login_failed", atorId, payload, ip)
        throw CredenciaisInvalidasException()
    }

    companion object {
        private fun mascarar(usuario: Usuario): String {
            if (usuario.grr != null) {
                return IdentificadorLogin.tryParse(usuario.grr!!.value)?.mascarado() ?: "***"
            }
            return IdentificadorLogin.tryParse(usuario.emailInstitucional.value)?.mascarado() ?: "***"
        }
    }
}
