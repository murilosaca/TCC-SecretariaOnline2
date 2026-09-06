package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.application.ports.OpaqueTokenHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final OpaqueTokenHasher opaqueTokenHasher;

    public LogoutUseCase(
            RefreshTokenRepository refreshTokenRepository,
            OpaqueTokenHasher opaqueTokenHasher
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.opaqueTokenHasher = opaqueTokenHasher;
    }

    @Transactional
    public void execute(String refreshRaw) {
        if (refreshRaw == null || refreshRaw.isBlank()) {
            return;
        }
        refreshTokenRepository.lockByTokenHash(opaqueTokenHasher.hash(refreshRaw))
                .ifPresent(sessao -> {
                    sessao.revogar(OffsetDateTime.now());
                    refreshTokenRepository.save(sessao);
                });
    }
}
