package br.ufpr.sept.so2.modules.iam.application

object DispositivoLabel {
    fun de(userAgent: String?): String {
        if (userAgent.isNullOrBlank()) {
            return "Dispositivo"
        }
        val aparelho = aparelho(userAgent)
        val navegador = navegador(userAgent)
        return if (navegador == null) aparelho else "$aparelho · $navegador"
    }

    private fun aparelho(userAgent: String): String =
        when {
            userAgent.contains("iPhone") -> "iPhone"
            userAgent.contains("iPad") -> "iPad"
            userAgent.contains("Android") -> "Android"
            userAgent.contains("Windows") -> "Windows"
            userAgent.contains("Mac OS") -> "Mac"
            userAgent.contains("Linux") -> "Linux"
            else -> "Dispositivo"
        }

    private fun navegador(userAgent: String): String? =
        when {
            userAgent.contains("Edg/") -> "Edge"
            userAgent.contains("Chrome/") -> "Chrome"
            userAgent.contains("Firefox/") -> "Firefox"
            userAgent.contains("Safari/") -> "Safari"
            else -> null
        }
}
