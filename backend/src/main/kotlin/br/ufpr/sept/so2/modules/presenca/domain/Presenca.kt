package br.ufpr.sept.so2.modules.presenca.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.OffsetDateTime
import java.util.UUID

class Presenca(
    val id: UUID,
    val eventoId: UUID,
    val usuarioId: UUID,
    val fase: FasePresenca,
    deviceUuid: String,
    val instante: OffsetDateTime,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    val deviceUuid: String = deviceUuid.trim().also {
        if (it.isBlank()) {
            throw DadoInvalidoException("Identificador do dispositivo é obrigatório.")
        }
    }

    companion object {
        fun registrar(
            id: UUID,
            eventoId: UUID,
            usuarioId: UUID,
            fase: FasePresenca,
            deviceUuid: String,
            agora: OffsetDateTime,
        ): Presenca = Presenca(id, eventoId, usuarioId, fase, deviceUuid, agora, agora, agora)
    }
}
