package br.ufpr.sept.so2.modules.presenca.infrastructure

import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class HostPinStore : HostPinPort {
    private val pins = ConcurrentHashMap<UUID, String>()

    override fun guardar(eventoId: UUID, pin: String) {
        if (pin.isBlank()) {
            return
        }
        pins[eventoId] = pin
    }

    override fun obter(eventoId: UUID): Optional<String> = Optional.ofNullable(pins[eventoId])

    override fun limpar(eventoId: UUID) {
        pins.remove(eventoId)
    }
}
