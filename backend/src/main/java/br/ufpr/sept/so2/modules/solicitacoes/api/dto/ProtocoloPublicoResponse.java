package br.ufpr.sept.so2.modules.solicitacoes.api.dto;

import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao;

public record ProtocoloPublicoResponse(
        String protocolo,
        String tipoNome,
        String estado,
        String hashSha256
) {

    public static ProtocoloPublicoResponse from(Solicitacao solicitacao) {
        return new ProtocoloPublicoResponse(
                solicitacao.getProtocolo().getValor(),
                solicitacao.getTipoNome(),
                solicitacao.getEstado(),
                solicitacao.getHashSha256()
        );
    }
}
