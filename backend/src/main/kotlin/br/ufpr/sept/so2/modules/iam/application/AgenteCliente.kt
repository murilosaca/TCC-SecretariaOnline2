package br.ufpr.sept.so2.modules.iam.application

object AgenteCliente {
    const val MAXIMO: Int = 300

    fun truncar(valor: String?): String? {
        val limpo = valor?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return if (limpo.length <= MAXIMO) limpo else limpo.substring(0, MAXIMO)
    }
}
