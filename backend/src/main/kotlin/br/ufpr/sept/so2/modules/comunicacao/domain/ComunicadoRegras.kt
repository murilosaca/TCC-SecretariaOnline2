package br.ufpr.sept.so2.modules.comunicacao.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.OffsetDateTime

object ComunicadoRegras {
    const val TITULO_MAX = 200
    const val CORPO_MAX = 20_000

    fun titulo(raw: String?): String {
        val titulo = raw?.trim().orEmpty()
        if (titulo.isEmpty()) {
            throw DadoInvalidoException("Informe o título do comunicado.")
        }
        if (titulo.length > TITULO_MAX) {
            throw DadoInvalidoException("O título deve ter no máximo $TITULO_MAX caracteres.")
        }
        return titulo
    }

    fun corpo(raw: String?): String {
        val corpo = raw?.trim().orEmpty()
        if (corpo.isEmpty()) {
            throw DadoInvalidoException("Informe o corpo do comunicado.")
        }
        if (corpo.length > CORPO_MAX) {
            throw DadoInvalidoException("O corpo deve ter no máximo $CORPO_MAX caracteres.")
        }
        return corpo
    }

    fun expiracao(valor: OffsetDateTime?, agora: OffsetDateTime): OffsetDateTime? {
        if (valor == null) {
            return null
        }
        if (!valor.isAfter(agora)) {
            throw DadoInvalidoException("A expiração precisa ser futura.")
        }
        return valor
    }
}
