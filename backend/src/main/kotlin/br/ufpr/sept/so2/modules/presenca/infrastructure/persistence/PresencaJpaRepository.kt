package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PresencaJpaRepository : JpaRepository<PresencaJpaEntity, UUID> {
    fun existsByEventoIdAndUsuarioIdAndFase(eventoId: UUID, usuarioId: UUID, fase: String): Boolean

    fun existsByEventoIdAndDeviceUuidAndUsuarioIdNot(eventoId: UUID, deviceUuid: String, usuarioId: UUID): Boolean

    fun findByEventoIdAndUsuarioId(eventoId: UUID, usuarioId: UUID): List<PresencaJpaEntity>

    fun countByEventoId(eventoId: UUID): Long
}
