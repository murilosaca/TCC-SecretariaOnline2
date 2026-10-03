package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

object NomeTemplate {
    private val PADRAO = Regex("^[a-z0-9]+(?:-[a-z0-9]+)*(?:\\.[a-z0-9]+(?:-[a-z0-9]+)*)+$")

    fun normalizar(bruto: String?): String {
        val nome = bruto?.trim()?.lowercase().orEmpty()
        if (nome.length > 120 || !PADRAO.matches(nome)) {
            throw DadoInvalidoException(
                "Nome do template deve estar em dot-notation, por exemplo boas-vindas.egresso.",
            )
        }
        return nome
    }
}

object TextoTemplate {
    fun exigir(valor: String?, campo: String, maximo: Int): String {
        val texto = valor?.trim().orEmpty()
        if (texto.isEmpty()) {
            throw DadoInvalidoException("$campo é obrigatório.")
        }
        if (texto.length > maximo) {
            throw DadoInvalidoException("$campo é longo demais.")
        }
        return texto
    }
}

/**
 * Placeholders fixos do editor. O preview de exemplo é só no cliente.
 * O dispatcher substitui com os valores do payload do evento.
 */
object PlaceholdersTemplate {
    val NOMES: List<String> = listOf("nome", "protocolo", "curso", "link", "data")
    private val TOKEN = Regex("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}")

    fun aplicar(texto: String, valores: Map<String, String>): String =
        TOKEN.replace(texto) { match ->
            val chave = match.groupValues[1]
            valores[chave] ?: match.value
        }
}
