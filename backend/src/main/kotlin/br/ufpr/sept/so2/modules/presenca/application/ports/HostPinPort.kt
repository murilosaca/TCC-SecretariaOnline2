package br.ufpr.sept.so2.modules.presenca.application.ports

import java.util.Optional
import java.util.UUID

interface HostPinPort {
    fun guardar(eventoId: UUID, pin: String)

    fun obter(eventoId: UUID): Optional<String>

    fun limpar(eventoId: UUID)
}
