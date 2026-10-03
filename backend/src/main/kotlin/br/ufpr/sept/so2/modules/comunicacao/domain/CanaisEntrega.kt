package br.ufpr.sept.so2.modules.comunicacao.domain

import java.time.LocalTime
import java.time.ZoneId

data class PreferenciaEntrega(
    val emailLigado: Boolean,
    val inAppLigado: Boolean,
    val dndInicio: LocalTime?,
    val dndFim: LocalTime?,
    val digestResumo: Boolean,
)

/**
 * E-mail e in-app desta fatia. Sem push.
 * CRITICAL ignora DND, digest e desligamento de canal.
 * DND e digest RESUMO seguram só o e-mail: o hub (in-app) continua visível.
 */
object CanaisEntrega {
    val ZONA: ZoneId = ZoneId.of("America/Sao_Paulo")

    fun resolver(
        prioridade: PrioridadeComunicacao,
        preferencia: PreferenciaEntrega?,
        agora: LocalTime,
    ): Set<CanalEntrega> {
        if (prioridade == PrioridadeComunicacao.CRITICAL) {
            return setOf(CanalEntrega.EMAIL, CanalEntrega.IN_APP)
        }
        val pref = preferencia ?: PADRAO
        val canais = linkedSetOf<CanalEntrega>()
        if (pref.inAppLigado) {
            canais.add(CanalEntrega.IN_APP)
        }
        if (emailImediato(pref, agora)) {
            canais.add(CanalEntrega.EMAIL)
        }
        return canais
    }

    fun emDnd(preferencia: PreferenciaEntrega, agora: LocalTime): Boolean {
        val inicio = preferencia.dndInicio ?: return false
        val fim = preferencia.dndFim ?: return false
        if (inicio == fim) {
            return false
        }
        return if (inicio.isBefore(fim)) {
            !agora.isBefore(inicio) && agora.isBefore(fim)
        } else {
            !agora.isBefore(inicio) || agora.isBefore(fim)
        }
    }

    private fun emailImediato(preferencia: PreferenciaEntrega, agora: LocalTime): Boolean =
        preferencia.emailLigado && !preferencia.digestResumo && !emDnd(preferencia, agora)

    private val PADRAO = PreferenciaEntrega(
        emailLigado = true,
        inAppLigado = true,
        dndInicio = null,
        dndFim = null,
        digestResumo = false,
    )
}
