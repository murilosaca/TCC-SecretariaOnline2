package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence

import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.modules.presenca.domain.Presenca
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class PresencaJpaAdapter(
    private val jpaRepository: PresencaJpaRepository,
    private val dispositivoRepository: PresencaDispositivoJpaRepository,
) : PresencaRepository {

    override fun save(presenca: Presenca): Presenca = jpaRepository.save(PresencaJpaEntity.fromDomain(presenca)).toDomain()

    override fun existsByEventoUsuarioFase(eventoId: UUID, usuarioId: UUID, fase: FasePresenca): Boolean =
        jpaRepository.existsByEventoIdAndUsuarioIdAndFase(eventoId, usuarioId, fase.name)

    override fun existsByEventoAndDeviceDeOutroUsuario(eventoId: UUID, deviceUuid: String, usuarioId: UUID): Boolean =
        jpaRepository.existsByEventoIdAndDeviceUuidAndUsuarioIdNot(eventoId, deviceUuid, usuarioId)

    override fun vincularDispositivo(eventoId: UUID, deviceUuid: String, usuarioId: UUID): Boolean {
        val id = PresencaDispositivoId(eventoId, deviceUuid)
        val existente = dispositivoRepository.findById(id)
        if (existente.isPresent) {
            return existente.get().usuarioId == usuarioId
        }
        val novo = PresencaDispositivoJpaEntity()
        novo.eventoId = eventoId
        novo.deviceUuid = deviceUuid
        novo.usuarioId = usuarioId
        novo.createdAt = OffsetDateTime.now()
        return try {
            dispositivoRepository.saveAndFlush(novo)
            true
        } catch (_: DataIntegrityViolationException) {
            dispositivoRepository.findById(id).map { it.usuarioId == usuarioId }.orElse(false)
        }
    }

    override fun usuariosComFase(eventoId: UUID, fase: FasePresenca): Set<UUID> =
        jpaRepository.findUsuarioIdsByEventoAndFase(eventoId, fase.name).toSet()

    override fun findByEventoAndUsuario(eventoId: UUID, usuarioId: UUID): List<Presenca> =
        jpaRepository.findByEventoIdAndUsuarioId(eventoId, usuarioId).map { it.toDomain() }

    override fun countByEvento(eventoId: UUID): Long = jpaRepository.countByEventoId(eventoId)
}
