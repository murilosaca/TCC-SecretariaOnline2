package br.ufpr.sept.so2.modules.iam.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException

class SenhaReutilizadaException(message: String) : DadoInvalidoException(message) {
    companion object {
        fun historico(): SenhaReutilizadaException =
            SenhaReutilizadaException(
                "Esta senha já foi utilizada recentemente. Escolha uma senha diferente.",
            )

        fun temporaria(): SenhaReutilizadaException =
            SenhaReutilizadaException(
                "A nova senha não pode ser igual à senha temporária gerada pelo sistema.",
            )
    }
}
