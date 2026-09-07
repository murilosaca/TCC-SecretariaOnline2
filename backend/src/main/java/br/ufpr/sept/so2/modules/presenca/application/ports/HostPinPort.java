package br.ufpr.sept.so2.modules.presenca.application.ports;

import java.util.Optional;
import java.util.UUID;

public interface HostPinPort {

    void guardar(UUID eventoId, String pin);

    Optional<String> obter(UUID eventoId);

    void limpar(UUID eventoId);
}
