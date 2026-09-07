package br.ufpr.sept.so2.modules.presenca.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

enum class AttendanceMode {
    QR_SINGLE,
    QR_DUAL,
    SECRET_SINGLE,
    SECRET_DUAL,
    ;

    fun isDual(): Boolean = this == QR_DUAL || this == SECRET_DUAL

    fun isSecret(): Boolean = this == SECRET_SINGLE || this == SECRET_DUAL

    fun exercitadoNesteSprint(): Boolean = this == SECRET_SINGLE

    companion object {
        fun from(raw: String?): AttendanceMode {
            if (raw.isNullOrBlank()) {
                throw DadoInvalidoException("Modo de presença obrigatório.")
            }
            return try {
                valueOf(raw.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                throw DadoInvalidoException("Modo de presença inválido.")
            }
        }
    }
}
