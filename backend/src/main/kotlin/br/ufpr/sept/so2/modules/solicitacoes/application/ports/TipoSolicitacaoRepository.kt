package br.ufpr.sept.so2.modules.solicitacoes.application.ports

import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

interface TipoSolicitacaoRepository {

    fun save(tipo: TipoSolicitacao): TipoSolicitacao

    fun findById(id: UUID): Optional<TipoSolicitacao>

    fun findByCodigo(codigo: String): Optional<TipoSolicitacao>

    fun findPublished(pageable: Pageable): Page<TipoSolicitacao>
}
