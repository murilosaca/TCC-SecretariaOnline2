package br.ufpr.sept.so2.modules.iam.api.dto

import br.ufpr.sept.so2.modules.iam.application.ConsultarSessaoUseCase
import br.ufpr.sept.so2.modules.iam.application.MenuLinks
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class SessaoResponse(
    val id: UUID,
    val mustChangePassword: Boolean,
    val authorities: List<String>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(sessao: ConsultarSessaoUseCase.SessaoAtual): SessaoResponse =
            SessaoResponse(
                sessao.id,
                sessao.mustChangePassword,
                sessao.authorities,
                MenuLinks.from(sessao.authorities, sessao.cursoConfigurarId),
            )
    }
}
