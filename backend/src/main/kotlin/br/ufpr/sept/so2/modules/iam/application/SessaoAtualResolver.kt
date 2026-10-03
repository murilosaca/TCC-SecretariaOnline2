package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.OpaqueTokenHasher
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SessaoAtualResolver(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val opaqueTokenHasher: OpaqueTokenHasher,
) {
    fun resolver(sessionId: UUID?, refreshRaw: String?, usuarioId: UUID): UUID? {
        if (sessionId != null) {
            return sessionId
        }
        if (refreshRaw.isNullOrBlank()) {
            return null
        }
        val sessao = refreshTokenRepository.findByTokenHash(opaqueTokenHasher.hash(refreshRaw)).orElse(null)
            ?: return null
        if (sessao.usuarioId != usuarioId || sessao.revoked) {
            return null
        }
        return sessao.id
    }
}
