package br.ufpr.sept.so2.modules.presenca.infrastructure

import br.ufpr.sept.so2.modules.presenca.application.ports.HostPinPort
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class HostPinStore : HostPinPort {
    private val pins = ConcurrentHashMap<UUID, Entrada>()

    override fun guardar(eventoId: UUID, pin: String) {
        if (pin.isBlank()) {
            return
        }
        pins[eventoId] = Entrada(pin, Instant.now())
    }

    override fun obter(eventoId: UUID): Optional<String> = Optional.ofNullable(pins[eventoId]?.valor)

    override fun emitidoEm(eventoId: UUID): Optional<Instant> = Optional.ofNullable(pins[eventoId]?.emitidoEm)

    override fun limpar(eventoId: UUID) {
        pins.remove(eventoId)
    }

    private data class Entrada(val valor: String, val emitidoEm: Instant)
}
