package br.ufpr.sept.so2.modules.estagio.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.OffsetDateTime
import java.util.UUID

class ParecerEstagio(
    val id: UUID,
    val idDocumento: UUID,
    val idAutor: UUID,
    val acao: String,
    val texto: String,
    val createdAt: OffsetDateTime,
) {
    companion object {
        const val REPROVAR_MIN = 20
        const val TEXTO_MAX = 2000

        fun validarTexto(acao: AcaoParecerEstagio, parecer: String?): String {
            val limpo = parecer?.trim().orEmpty()
            if (acao == AcaoParecerEstagio.REPROVAR && limpo.length < REPROVAR_MIN) {
                throw DadoInvalidoException(
                    "Informe o parecer para reprovação (mín. $REPROVAR_MIN caracteres).",
                )
            }
            if (limpo.isEmpty()) {
                throw DadoInvalidoException("Informe o parecer.")
            }
            if (limpo.length > TEXTO_MAX) {
                throw DadoInvalidoException("O parecer excede $TEXTO_MAX caracteres.")
            }
            return limpo
        }
    }
}
