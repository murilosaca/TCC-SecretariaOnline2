package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.JtiBlacklistRepository
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository
import br.ufpr.sept.so2.modules.iam.application.ports.SenhaHistoricoRepository
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.PoliticaSenha
import br.ufpr.sept.so2.modules.iam.domain.SenhaReutilizadaException
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.exception.TokenResetInvalidoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class RedefinirSenhaUseCase(
    private val jwtTokenService: JwtTokenService,
    private val jtiBlacklistRepository: JtiBlacklistRepository,
    private val usuarioRepository: UsuarioRepository,
    private val senhaHistoricoRepository: SenhaHistoricoRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordHasher: PasswordHasher,
    private val auditLogPort: AuditLogPort,
    private val settings: IamSettings,
) {
    @Transactional
    fun execute(token: String, novaSenha: String, ip: String?) {
        val claims = lerToken(token)
        if (jtiBlacklistRepository.contains(claims.jti)) {
            throw TokenResetInvalidoException()
        }
        PoliticaSenha.validar(novaSenha)
        val usuario = usuarioRepository.findById(claims.userId)
            .orElseThrow { TokenResetInvalidoException() }
        rejeitarSeReutilizada(usuario, novaSenha)

        val agora = OffsetDateTime.now()
        val novoHash = passwordHasher.hash(novaSenha)
        senhaHistoricoRepository.append(usuario.id, usuario.senhaHash)
        usuario.redefinirSenha(novoHash, agora)
        usuarioRepository.save(usuario)
        refreshTokenRepository.revokeAllByUsuarioId(usuario.id)
        jtiBlacklistRepository.add(claims.jti, agora.plusSeconds(settings.resetTtlSeconds))
        auditLogPort.append("iam.password_reset_completed", usuario.id, "ok", ip)
    }

    fun validarToken(token: String) {
        val claims = lerToken(token)
        if (jtiBlacklistRepository.contains(claims.jti)) {
            throw TokenResetInvalidoException()
        }
        usuarioRepository.findById(claims.userId).orElseThrow { TokenResetInvalidoException() }
    }

    private fun lerToken(token: String): JwtTokenService.ResetTokenClaims =
        try {
            jwtTokenService.parseResetToken(token)
        } catch (_: RuntimeException) {
            throw TokenResetInvalidoException()
        }

    private fun rejeitarSeReutilizada(usuario: Usuario, novaSenha: String) {
        val hashes = ArrayList<String>()
        hashes.add(usuario.senhaHash)
        hashes.addAll(senhaHistoricoRepository.findLastHashes(usuario.id, 3))
        for (hash in hashes) {
            if (passwordHasher.matches(novaSenha, hash)) {
                throw SenhaReutilizadaException.historico()
            }
        }
    }
}
