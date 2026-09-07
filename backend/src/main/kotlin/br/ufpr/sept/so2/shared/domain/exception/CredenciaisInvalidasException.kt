package br.ufpr.sept.so2.shared.domain.exception

class CredenciaisInvalidasException : RuntimeException(MENSAGEM) {
    companion object {
        const val MENSAGEM: String =
            "Credenciais inválidas. Verifique seus dados e tente novamente."
    }
}
