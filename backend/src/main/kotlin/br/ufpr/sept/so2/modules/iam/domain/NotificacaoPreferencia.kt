package br.ufpr.sept.so2.modules.iam.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

enum class PrioridadeNotificacao {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
}

enum class CanalNotificacao {
    EMAIL,
    IN_APP,
}

enum class ModoDigest {
    IMEDIATO,
    RESUMO,
}

class NotificacaoPreferencia(
    val usuarioId: UUID,
    var canais: Map<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>>,
    var dndInicio: LocalTime?,
    var dndFim: LocalTime?,
    var digest: ModoDigest,
    val createdAt: OffsetDateTime,
    var updatedAt: OffsetDateTime,
)

object NotificacaoPreferenciaRegras {
    fun padrao(usuarioId: UUID, agora: OffsetDateTime): NotificacaoPreferencia =
        NotificacaoPreferencia(
            usuarioId,
            matrizTudoLigado(),
            null,
            null,
            ModoDigest.IMEDIATO,
            agora,
            agora,
        )

    fun aplicar(
        atual: NotificacaoPreferencia,
        canais: Map<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>>?,
        dndInicioPresente: Boolean,
        dndInicio: LocalTime?,
        dndFimPresente: Boolean,
        dndFim: LocalTime?,
        digest: ModoDigest?,
        agora: OffsetDateTime,
    ): NotificacaoPreferencia {
        val mesclados = mesclarCanais(atual.canais, canais)
        exigirCritical(mesclados)
        val inicio = if (dndInicioPresente) dndInicio else atual.dndInicio
        val fim = if (dndFimPresente) dndFim else atual.dndFim
        if ((inicio == null) != (fim == null)) {
            throw DadoInvalidoException("Informe o início e o fim do horário de não perturbe.")
        }
        atual.canais = mesclados
        atual.dndInicio = inicio
        atual.dndFim = fim
        atual.digest = digest ?: atual.digest
        atual.updatedAt = agora
        return atual
    }

    private fun mesclarCanais(
        atual: Map<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>>,
        patch: Map<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>>?,
    ): Map<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>> {
        val resultado = linkedMapOf<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>>()
        for (prioridade in PrioridadeNotificacao.entries) {
            val base = atual[prioridade] ?: CanalNotificacao.entries.associateWith { true }
            val alteracao = patch?.get(prioridade)
            val canais = linkedMapOf<CanalNotificacao, Boolean>()
            for (canal in CanalNotificacao.entries) {
                canais[canal] = alteracao?.get(canal) ?: base[canal] ?: true
            }
            resultado[prioridade] = canais
        }
        return resultado
    }

    private fun exigirCritical(canais: Map<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>>) {
        val critical = canais[PrioridadeNotificacao.CRITICAL].orEmpty()
        if (CanalNotificacao.entries.any { critical[it] != true }) {
            throw DadoInvalidoException("Notificações de prioridade CRITICAL não podem ser desabilitadas.")
        }
    }

    private fun matrizTudoLigado(): Map<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>> =
        PrioridadeNotificacao.entries.associateWith {
            CanalNotificacao.entries.associateWith { true }
        }
}
