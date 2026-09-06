package br.ufpr.sept.so2.modules.iam.application;

public record LoginResult(
        String accessToken,
        String refreshToken,
        boolean mustChangePassword,
        long expiresIn
) {
}
