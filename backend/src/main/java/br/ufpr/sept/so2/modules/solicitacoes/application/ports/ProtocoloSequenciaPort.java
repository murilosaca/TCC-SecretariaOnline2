package br.ufpr.sept.so2.modules.solicitacoes.application.ports;

import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo;

public interface ProtocoloSequenciaPort {

    Protocolo proximo(int ano);
}
