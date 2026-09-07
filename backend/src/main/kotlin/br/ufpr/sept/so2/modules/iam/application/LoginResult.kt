package br.ufpr.sept.so2.modules.iam.application

data class LoginResult(
    val accessToken: String,
    val refreshToken: String,
    val mustChangePassword: Boolean,
    val expiresIn: Long,
)
