package br.ufpr.sept.so2.shared.domain.exception

class TokenAcaoInvalidoException : RuntimeException(MENSAGEM) {
    companion object {
        const val MENSAGEM: String = "Link inválido ou expirado."
    }
}
