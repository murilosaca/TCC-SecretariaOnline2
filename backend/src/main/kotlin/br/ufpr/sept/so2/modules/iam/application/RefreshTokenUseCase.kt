package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.modules.iam.application.ports.OpaqueTokenHasher
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao
import br.ufpr.sept.so2.shared.domain.exception.CredenciaisInvalidasException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class RefreshTokenUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val usuarioRepository: UsuarioRepository,
    private val jwtTokenService: JwtTokenService,
    private val opaqueTokenHasher: OpaqueTokenHasher,
    private val auditLogPort: AuditLogPort,
    private val settings: IamSettings,
) {
    @Transactional
    fun execute(refreshRaw: String?, ip: String?): LoginResult {
        if (refreshRaw.isNullOrBlank()) {
            throw CredenciaisInvalidasException()
        }
        val agora = OffsetDateTime.now()
        val sessao = refreshTokenRepository.lockByTokenHash(opaqueTokenHasher.hash(refreshRaw))
            .orElseThrow { CredenciaisInvalidasException() }

        if (sessao.reutilizadaOuRevogada()) {
            refreshTokenRepository.revokeAllByUsuarioId(sessao.usuarioId)
            auditLogPort.append("iam.suspicious_token_reuse", sessao.usuarioId, "reuse", ip)
            throw CredenciaisInvalidasException()
        }
        if (sessao.expirada(agora)) {
            sessao.revogar(agora)
            refreshTokenRepository.save(sessao)
            throw CredenciaisInvalidasException()
        }

        val usuario = usuarioRepository.findById(sessao.usuarioId)
            .orElseThrow { CredenciaisInvalidasException() }
        if (!usuario.ativo) {
            throw CredenciaisInvalidasException()
        }

        sessao.marcarUsada(agora)
        refreshTokenRepository.save(sessao)

        val novoRaw = UUID.randomUUID().toString()
        val nova = RefreshSessao(
            Uuids.v7(),
            usuario.id,
            opaqueTokenHasher.hash(novoRaw),
            agora.plusSeconds(settings.refreshTtlSeconds),
            false,
            false,
            agora,
            agora,
        )
        refreshTokenRepository.save(nova)
        return LoginResult(
            jwtTokenService.emitAccessToken(usuario),
            novoRaw,
            usuario.precisaPrimeiroAcesso(),
            settings.accessTtlSeconds,
        )
    }
}
