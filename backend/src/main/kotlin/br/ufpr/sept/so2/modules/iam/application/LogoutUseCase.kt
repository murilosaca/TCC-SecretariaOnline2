package br.ufpr.sept.so2.modules.iam.application

import br.ufpr.sept.so2.modules.iam.application.ports.OpaqueTokenHasher
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class LogoutUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val opaqueTokenHasher: OpaqueTokenHasher,
) {
    @Transactional
    fun execute(refreshRaw: String?) {
        if (refreshRaw.isNullOrBlank()) {
            return
        }
        refreshTokenRepository.lockByTokenHash(opaqueTokenHasher.hash(refreshRaw))
            .ifPresent { sessao ->
                sessao.revogar(OffsetDateTime.now())
                refreshTokenRepository.save(sessao)
            }
    }
}
