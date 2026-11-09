package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface PresencaJpaRepository : JpaRepository<PresencaJpaEntity, UUID> {
    fun existsByEventoIdAndUsuarioIdAndFase(eventoId: UUID, usuarioId: UUID, fase: String): Boolean

    fun existsByEventoIdAndDeviceUuidAndUsuarioIdNot(eventoId: UUID, deviceUuid: String, usuarioId: UUID): Boolean

    @Query(
        """
            SELECT DISTINCT p.usuarioId FROM PresencaJpaEntity p
            WHERE p.eventoId = :eventoId AND p.fase = :fase
        """,
    )
    fun findUsuarioIdsByEventoAndFase(
        @Param("eventoId") eventoId: UUID,
        @Param("fase") fase: String,
    ): List<UUID>

    fun findByEventoIdAndUsuarioId(eventoId: UUID, usuarioId: UUID): List<PresencaJpaEntity>

    fun countByEventoId(eventoId: UUID): Long
}
