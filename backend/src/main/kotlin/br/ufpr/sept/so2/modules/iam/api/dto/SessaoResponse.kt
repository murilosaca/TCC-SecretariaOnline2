package br.ufpr.sept.so2.modules.iam.api.dto

import br.ufpr.sept.so2.modules.iam.application.ConsultarSessaoUseCase
import java.util.UUID

data class SessaoResponse(
    val id: UUID,
    val mustChangePassword: Boolean,
    val authorities: List<String>,
) {
    companion object {
        fun from(sessao: ConsultarSessaoUseCase.SessaoAtual): SessaoResponse =
            SessaoResponse(sessao.id, sessao.mustChangePassword, sessao.authorities)
    }
}
