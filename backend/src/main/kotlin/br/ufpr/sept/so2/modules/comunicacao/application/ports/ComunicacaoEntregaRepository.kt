package br.ufpr.sept.so2.modules.comunicacao.application.ports

import br.ufpr.sept.so2.modules.comunicacao.domain.ComunicacaoEntrega
import java.util.UUID

interface ComunicacaoEntregaRepository {
    fun save(entrega: ComunicacaoEntrega): ComunicacaoEntrega

    fun findVisiveis(destinatarioId: UUID): List<ComunicacaoEntrega>

    fun findByComunicacaoEDestinatario(comunicacaoId: UUID, destinatarioId: UUID): ComunicacaoEntrega?
}
