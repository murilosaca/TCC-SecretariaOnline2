package br.ufpr.sept.so2.modules.iam.api.dto;

import br.ufpr.sept.so2.modules.iam.application.ConsultarSessaoUseCase;

import java.util.List;
import java.util.UUID;

public record SessaoResponse(UUID id, boolean mustChangePassword, List<String> authorities) {

    public static SessaoResponse from(ConsultarSessaoUseCase.SessaoAtual sessao) {
        return new SessaoResponse(sessao.id(), sessao.mustChangePassword(), sessao.authorities());
    }
}
