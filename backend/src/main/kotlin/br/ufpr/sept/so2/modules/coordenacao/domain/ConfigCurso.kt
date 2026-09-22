package br.ufpr.sept.so2.modules.coordenacao.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.util.UUID

enum class BancaModalidade {
    PRESENCIAL,
    REMOTO,
    HIBRIDO,
    ;

    companion object {
        fun from(raw: String?): BancaModalidade {
            if (raw.isNullOrBlank()) {
                throw DadoInvalidoException("Modalidade da banca inválida.")
            }
            return when (raw.trim().uppercase()) {
                "PRESENCIAL" -> PRESENCIAL
                "REMOTO" -> REMOTO
                "HIBRIDO", "HÍBRIDO" -> HIBRIDO
                else -> throw DadoInvalidoException("Modalidade da banca inválida.")
            }
        }
    }
}

data class ConfigCurso(
    val id: UUID,
    val nome: String,
    val sigla: String,
    val horasFormativasMinimas: Int,
    val duracaoCalendario: Int,
    val bancaMembrosExternos: Int,
    val bancaModalidade: BancaModalidade,
    val regimento: String,
) {
    companion object {
        const val DURACAO_PADRAO: Int = 15
        const val MEMBROS_PADRAO: Int = 1
        const val REGIMENTO_PADRAO: String = ""
    }
}

data class ConfigCursoPatch(
    val horasFormativasMinimas: Int? = null,
    val duracaoCalendario: Int? = null,
    val bancaMembrosExternos: Int? = null,
    val bancaModalidade: String? = null,
    val regimento: String? = null,
) {
    fun vazio(): Boolean =
        horasFormativasMinimas == null &&
            duracaoCalendario == null &&
            bancaMembrosExternos == null &&
            bancaModalidade == null &&
            regimento == null
}

data class CampoAlterado(
    val campo: String,
    val de: Any?,
    val para: Any?,
)
