package br.ufpr.sept.so2.modules.certificados.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class CertificadoTipo {
    FORMATIVA,
    EVENTO,
    ;

    companion object {
        fun from(valor: String?): CertificadoTipo {
            val normalizado = valor?.trim()?.uppercase().orEmpty()
            return entries.find { it.name == normalizado }
                ?: throw DadoInvalidoException("Tipo de certificado inválido.")
        }
    }
}
