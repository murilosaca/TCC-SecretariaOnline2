package br.ufpr.sept.so2.modules.iam.api.dto;

import br.ufpr.sept.so2.modules.iam.application.LoginResult;

public record LoginResponse(
        String accessToken,
        boolean mustChangePassword,
        long expiresIn,
        String tokenType
) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.accessToken(), result.mustChangePassword(), result.expiresIn(), "Bearer");
    }
}
