package br.ufpr.sept.so2.modules.iam.application.ports;

import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    RefreshSessao save(RefreshSessao sessao);

    Optional<RefreshSessao> lockByTokenHash(String tokenHash);

    void revokeAllByUsuarioId(UUID usuarioId);
}
