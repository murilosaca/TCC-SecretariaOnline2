package br.ufpr.sept.so2.modules.presenca.application.ports

import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.modules.presenca.domain.Presenca
import java.util.UUID

interface PresencaRepository {
    fun save(presenca: Presenca): Presenca

    fun existsByEventoUsuarioFase(eventoId: UUID, usuarioId: UUID, fase: FasePresenca): Boolean

    fun existsByEventoAndDeviceDeOutroUsuario(eventoId: UUID, deviceUuid: String, usuarioId: UUID): Boolean

    /**
     * Amarra o dispositivo ao aluno neste evento. O mesmo aluno pode usar o
     * aparelho nas duas fases. Outro aluno no mesmo aparelho devolve false.
     */
    fun vincularDispositivo(eventoId: UUID, deviceUuid: String, usuarioId: UUID): Boolean

    fun usuariosComFase(eventoId: UUID, fase: FasePresenca): Set<UUID>

    fun findByEventoAndUsuario(eventoId: UUID, usuarioId: UUID): List<Presenca>

    fun countByEvento(eventoId: UUID): Long
}
