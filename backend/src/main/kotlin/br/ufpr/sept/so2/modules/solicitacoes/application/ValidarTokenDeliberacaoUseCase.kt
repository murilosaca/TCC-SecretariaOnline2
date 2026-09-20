package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.modules.iam.application.ports.JtiBlacklistRepository
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.shared.domain.exception.TokenAcaoInvalidoException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ValidarTokenDeliberacaoUseCase(
    private val jwtTokenService: JwtTokenService,
    private val jtiBlacklistRepository: JtiBlacklistRepository,
) {
    fun execute(token: String?, solicitacaoId: UUID, usuarioId: UUID): JwtTokenService.DeliberationTokenClaims? {
        if (token.isNullOrBlank()) {
            return null
        }
        val claims = try {
            jwtTokenService.parseDeliberationToken(token)
        } catch (_: RuntimeException) {
            throw TokenAcaoInvalidoException()
        }
        if (claims.userId != usuarioId || claims.solicitacaoId != solicitacaoId) {
            throw TokenAcaoInvalidoException()
        }
        if (jtiBlacklistRepository.contains(claims.jti)) {
            throw TokenAcaoInvalidoException()
        }
        return claims
    }
}
