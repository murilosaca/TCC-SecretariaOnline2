package br.ufpr.sept.so2.modules.presenca.infrastructure;

import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HostPinStore implements HostPinPort {

    private final ConcurrentHashMap<UUID, String> pins = new ConcurrentHashMap<>();

    public void guardar(UUID eventoId, String pin) {
        if (eventoId == null || pin == null || pin.isBlank()) {
            return;
        }
        pins.put(eventoId, pin);
    }

    public Optional<String> obter(UUID eventoId) {
        return Optional.ofNullable(pins.get(eventoId));
    }

    public void limpar(UUID eventoId) {
        pins.remove(eventoId);
    }
}
