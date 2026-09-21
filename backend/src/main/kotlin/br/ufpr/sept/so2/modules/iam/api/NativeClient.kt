package br.ufpr.sept.so2.modules.iam.api

import jakarta.servlet.http.HttpServletRequest

/**
 * Cliente nativo (Expo) não persiste cookie httpOnly. Identifica-se por
 * `X-SO2-Client: native` para receber `refreshToken` no JSON; o cookie
 * `so2_refresh` continua sendo setado para a web.
 */
object NativeClient {
    const val HEADER = "X-SO2-Client"
    const val VALUE = "native"

    fun requested(request: HttpServletRequest): Boolean =
        request.getHeader(HEADER)?.equals(VALUE, ignoreCase = true) == true

    /**
     * Body ganha do cookie: o app guarda o valor rotacionado no Keychain.
     * A web só envia cookie.
     */
    fun resolveRefreshToken(cookie: String?, body: String?): String? {
        val fromBody = body?.takeIf { it.isNotBlank() }
        if (fromBody != null) {
            return fromBody
        }
        return cookie?.takeIf { it.isNotBlank() }
    }
}
