package br.ufpr.sept.so2.modules.iam.infrastructure.security

import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RefreshCookieFactory(
    private val properties: IamProperties,
) {
    fun create(refreshToken: String): ResponseCookie =
        base()
            .value(refreshToken)
            .maxAge(Duration.ofSeconds(properties.refreshTtlSeconds))
            .build()

    fun clear(): ResponseCookie =
        base()
            .value("")
            .maxAge(Duration.ZERO)
            .build()

    fun cookieName(): String = properties.cookieName

    private fun base(): ResponseCookie.ResponseCookieBuilder =
        ResponseCookie.from(properties.cookieName, "")
            .httpOnly(true)
            .secure(properties.cookieSecure)
            .sameSite(properties.cookieSameSite)
            .path("/auth")
}
