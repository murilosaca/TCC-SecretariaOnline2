package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin

object EmailMascarado {
    private val EMAIL = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")

    fun de(email: String?): String =
        IdentificadorLogin.tryParse(email)?.mascarado() ?: "***"

    fun sanitizarErro(mensagem: String?): String {
        val raw = mensagem?.replace('\n', ' ')?.take(200).orEmpty()
        if (raw.isBlank()) {
            return "falha no despacho"
        }
        return EMAIL.replace(raw, "***")
    }
}
