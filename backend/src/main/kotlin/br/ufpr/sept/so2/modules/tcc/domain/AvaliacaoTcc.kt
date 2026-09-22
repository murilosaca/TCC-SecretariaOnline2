package br.ufpr.sept.so2.modules.tcc.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.OffsetDateTime
import java.util.UUID

class AvaliacaoTcc(
    val id: UUID,
    val idAutor: UUID,
    val papel: PapelBancaTcc,
    val acao: AcaoAvaliacaoTcc,
    val resultado: TccEstado,
    val nota: BigDecimal,
    val parecer: String,
    val createdAt: OffsetDateTime,
) {
    companion object {
        const val PARECER_MIN = 20
        const val PARECER_MAX = 2000
        private val NOTA_MAX = BigDecimal("10")

        fun validarTexto(acao: AcaoAvaliacaoTcc, parecer: String?): String {
            val limpo = parecer?.trim().orEmpty()
            if (acao.exigeParecerLongo() && limpo.length < PARECER_MIN) {
                throw DadoInvalidoException("O parecer precisa ter no mínimo $PARECER_MIN caracteres.")
            }
            if (limpo.isEmpty()) {
                throw DadoInvalidoException("Informe o parecer.")
            }
            if (limpo.length > PARECER_MAX) {
                throw DadoInvalidoException("O parecer excede $PARECER_MAX caracteres.")
            }
            return limpo
        }

        fun validarNota(nota: BigDecimal?): BigDecimal {
            if (nota == null) {
                throw DadoInvalidoException("Informe a nota de 0 a 10.")
            }
            val normalizada = nota.stripTrailingZeros()
            if (normalizada.scale() > 1) {
                throw DadoInvalidoException("A nota aceita no máximo uma casa decimal.")
            }
            if (normalizada < BigDecimal.ZERO || normalizada > NOTA_MAX) {
                throw DadoInvalidoException("Informe a nota de 0 a 10.")
            }
            return nota.setScale(1, RoundingMode.UNNECESSARY)
        }
    }
}
