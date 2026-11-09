package br.ufpr.sept.so2.modules.solicitacoes.application.ports

import br.ufpr.sept.so2.modules.solicitacoes.domain.SolicitacaoAnexo
import java.util.UUID

interface SolicitacaoAnexoRepository {
    fun save(anexo: SolicitacaoAnexo): SolicitacaoAnexo

    fun findBySolicitacaoId(solicitacaoId: UUID): List<SolicitacaoAnexo>
}
