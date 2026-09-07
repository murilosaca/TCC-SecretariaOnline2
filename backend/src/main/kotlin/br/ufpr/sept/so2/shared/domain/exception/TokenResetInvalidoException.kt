package br.ufpr.sept.so2.shared.domain.exception

class TokenResetInvalidoException : RuntimeException(MENSAGEM) {
    companion object {
        const val MENSAGEM: String = "Link inválido ou expirado."
    }
}
