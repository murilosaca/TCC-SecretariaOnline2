package br.ufpr.sept.so2.modules.solicitacoes.application.ports

import java.util.UUID

fun interface SolicitanteResumoPort {
    fun nomeDe(usuarioId: UUID): String?
}
