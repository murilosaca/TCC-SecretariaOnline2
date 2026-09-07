package br.ufpr.sept.so2.modules.iam.application.ports

import br.ufpr.sept.so2.modules.iam.domain.Usuario
import java.util.UUID

interface JwtTokenService {
    fun emitAccessToken(usuario: Usuario): String

    fun emitResetToken(usuario: Usuario): String

    fun parseAccessToken(token: String): AccessTokenClaims

    fun parseResetToken(token: String): ResetTokenClaims

    data class AccessTokenClaims(
        val userId: UUID,
        val authorities: List<String>,
        val mustChangePassword: Boolean,
        val jti: String,
    )

    data class ResetTokenClaims(
        val userId: UUID,
        val jti: String,
    )
}
