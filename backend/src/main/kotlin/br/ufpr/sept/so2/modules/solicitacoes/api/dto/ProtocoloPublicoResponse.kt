package br.ufpr.sept.so2.modules.solicitacoes.api.dto

import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao

data class ProtocoloPublicoResponse(
    val protocolo: String,
    val tipoNome: String,
    val estado: String,
    val hashSha256: String?,
) {
    companion object {
        @JvmStatic
        fun from(solicitacao: Solicitacao): ProtocoloPublicoResponse =
            ProtocoloPublicoResponse(
                solicitacao.protocolo.valor,
                solicitacao.tipoNome,
                solicitacao.estado,
                solicitacao.hashSha256,
            )
    }
}
