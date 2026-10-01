package br.ufpr.sept.so2.modules.bff.application

import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Escopo e SLA do dashboard da secretaria. A mensagem de curso vazio é
 * duplicada de propósito: o BFF não importa `diplomas` nem `reports`.
 */
internal object SecretariaDashboardRegras {
    const val MSG_SEM_CURSO: String = "Nenhum curso vinculado à sua secretaria foi encontrado."
    const val FILA_LIMITE: Int = 10
    const val AGENDA_LIMITE: Int = 10
    const val HORAS_WARNING: Long = 24
    const val SLA_DANGER: String = "danger"
    const val SLA_WARNING: String = "warning"
    const val ESTADO_CONCLUIDA: String = "CONCLUIDA"
    val ESTADOS_ABERTA: Set<String> = setOf("EM_ANALISE", "EM_AJUSTE")

    fun exigirCursos(cursoIds: Set<UUID>): Set<UUID> {
        if (cursoIds.isEmpty()) {
            throw AcessoNegadoException(MSG_SEM_CURSO)
        }
        return cursoIds
    }

    fun slaStatus(prazoEm: OffsetDateTime?, agora: OffsetDateTime): String? {
        if (prazoEm == null) {
            return null
        }
        if (prazoEm.isBefore(agora)) {
            return SLA_DANGER
        }
        if (prazoEm.isBefore(agora.plusHours(HORAS_WARNING))) {
            return SLA_WARNING
        }
        return null
    }

    fun inicioDoDia(agora: OffsetDateTime): OffsetDateTime =
        agora.toLocalDate().atStartOfDay().atOffset(agora.offset)
}
