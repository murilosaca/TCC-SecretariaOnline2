package br.ufpr.sept.so2.modules.solicitacoes.application.ports

import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo

interface ProtocoloSequenciaPort {

    fun proximo(ano: Int): Protocolo
}
