package br.ufpr.sept.so2.modules.iam.api

import jakarta.servlet.http.HttpServletRequest

object ClienteIp {
    fun de(request: HttpServletRequest): String {
        val forwarded = request.getHeader("X-Forwarded-For")
        if (!forwarded.isNullOrBlank()) {
            return forwarded.split(",")[0].trim()
        }
        return request.remoteAddr
    }
}
