package br.ufpr.sept.so2.modules.solicitacoes.application.ports

import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

interface SolicitacaoRepository {

    fun save(solicitacao: Solicitacao): Solicitacao

    fun findById(id: UUID): Optional<Solicitacao>

    fun findByProtocolo(protocolo: String): Optional<Solicitacao>

    fun findMinhas(
        solicitanteId: UUID,
        estado: String?,
        tipoCodigo: String?,
        ano: Int?,
        pageable: Pageable,
    ): Page<Solicitacao>
}
