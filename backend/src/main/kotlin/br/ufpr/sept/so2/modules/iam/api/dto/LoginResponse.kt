package br.ufpr.sept.so2.modules.iam.api.dto

import br.ufpr.sept.so2.modules.iam.application.LoginResult
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginResponse(
    val accessToken: String,
    val mustChangePassword: Boolean,
    val expiresIn: Long,
    val tokenType: String,
    val refreshToken: String? = null,
) {
    companion object {
        fun from(result: LoginResult, includeRefreshToken: Boolean = false): LoginResponse =
            LoginResponse(
                result.accessToken,
                result.mustChangePassword,
                result.expiresIn,
                "Bearer",
                if (includeRefreshToken) result.refreshToken else null,
            )
    }
}
