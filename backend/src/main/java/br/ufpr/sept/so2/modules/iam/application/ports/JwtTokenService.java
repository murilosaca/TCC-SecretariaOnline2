package br.ufpr.sept.so2.modules.iam.application.ports;

import br.ufpr.sept.so2.modules.iam.domain.Usuario;

import java.util.List;
import java.util.UUID;

public interface JwtTokenService {

    String emitAccessToken(Usuario usuario);

    String emitResetToken(Usuario usuario);

    AccessTokenClaims parseAccessToken(String token);

    ResetTokenClaims parseResetToken(String token);

    record AccessTokenClaims(
            UUID userId,
            List<String> authorities,
            boolean mustChangePassword,
            String jti
    ) {
    }

    record ResetTokenClaims(UUID userId, String jti) {
    }
}
