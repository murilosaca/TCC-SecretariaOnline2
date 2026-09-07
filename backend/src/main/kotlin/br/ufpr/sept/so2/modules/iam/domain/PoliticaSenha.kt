package br.ufpr.sept.so2.modules.iam.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

object PoliticaSenha {

    const val TAMANHO_MINIMO: Int = 12

    fun validar(senha: String?) {
        if (senha == null || senha.length < TAMANHO_MINIMO) {
            throw DadoInvalidoException("A senha deve ter no mínimo 12 caracteres.")
        }
        if (!temMaiuscula(senha)) {
            throw DadoInvalidoException("A senha deve conter ao menos uma letra maiúscula.")
        }
        if (!temMinuscula(senha)) {
            throw DadoInvalidoException("A senha deve conter ao menos uma letra minúscula.")
        }
        if (!temDigito(senha)) {
            throw DadoInvalidoException("A senha deve conter ao menos um número.")
        }
        if (!temEspecial(senha)) {
            throw DadoInvalidoException("A senha deve conter ao menos um caractere especial.")
        }
    }

    fun atende(senha: String?): Boolean =
        senha != null &&
            senha.length >= TAMANHO_MINIMO &&
            temMaiuscula(senha) &&
            temMinuscula(senha) &&
            temDigito(senha) &&
            temEspecial(senha)

    /**
     * Medidor de 4 níveis (F0.3 / F1.2): 0 vazia, 1 fraca, 2 parcial, 3 maioria, 4 forte.
     */
    fun pontuacao(senha: String?): Int {
        if (senha == null || senha.isEmpty()) {
            return 0
        }
        var criterios = 0
        if (senha.length >= TAMANHO_MINIMO) {
            criterios++
        }
        if (temMaiuscula(senha)) {
            criterios++
        }
        if (temMinuscula(senha)) {
            criterios++
        }
        if (temDigito(senha)) {
            criterios++
        }
        if (temEspecial(senha)) {
            criterios++
        }
        if (criterios <= 1 || senha.length < 8) {
            return 1
        }
        if (criterios <= 3) {
            return 2
        }
        if (criterios == 4) {
            return 3
        }
        return 4
    }

    fun temMaiuscula(senha: String): Boolean = senha.any { it.isUpperCase() }

    fun temMinuscula(senha: String): Boolean = senha.any { it.isLowerCase() }

    fun temDigito(senha: String): Boolean = senha.any { it.isDigit() }

    fun temEspecial(senha: String): Boolean = senha.any { !it.isLetterOrDigit() }
}
