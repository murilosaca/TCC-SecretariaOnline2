package br.ufpr.sept.so2.modules.comunicacao.application

import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim

interface OutboxEventoHandler {
    val tipos: Set<String>

    fun handle(evento: OutboxClaim)
}
