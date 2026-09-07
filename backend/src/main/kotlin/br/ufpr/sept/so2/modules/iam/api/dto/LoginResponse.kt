package br.ufpr.sept.so2.modules.iam.api.dto

import br.ufpr.sept.so2.modules.iam.application.LoginResult

data class LoginResponse(
    val accessToken: String,
    val mustChangePassword: Boolean,
    val expiresIn: Long,
    val tokenType: String,
) {
    companion object {
        fun from(result: LoginResult): LoginResponse =
            LoginResponse(result.accessToken, result.mustChangePassword, result.expiresIn, "Bearer")
    }
}
