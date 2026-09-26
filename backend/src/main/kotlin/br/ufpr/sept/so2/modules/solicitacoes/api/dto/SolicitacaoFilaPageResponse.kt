package br.ufpr.sept.so2.modules.solicitacoes.api.dto

import br.ufpr.sept.so2.shared.api.PageResponse
import com.fasterxml.jackson.annotation.JsonProperty

data class SolicitacaoFilaPageResponse(
    val content: List<SolicitacaoResponse>,
    val page: PageResponse.PageMeta,
    val deliberadores: List<DeliberadorOpcaoResponse>,
    @get:JsonProperty("_links")
    val links: PageResponse.PageLinks?,
)
