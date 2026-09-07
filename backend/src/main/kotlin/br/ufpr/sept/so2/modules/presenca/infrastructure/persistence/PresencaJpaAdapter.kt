package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence

import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.modules.presenca.domain.Presenca
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PresencaJpaAdapter(
    private val jpaRepository: PresencaJpaRepository,
) : PresencaRepository {

    override fun save(presenca: Presenca): Presenca = jpaRepository.save(PresencaJpaEntity.fromDomain(presenca)).toDomain()

    override fun existsByEventoUsuarioFase(eventoId: UUID, usuarioId: UUID, fase: FasePresenca): Boolean =
        jpaRepository.existsByEventoIdAndUsuarioIdAndFase(eventoId, usuarioId, fase.name)

    override fun existsByEventoAndDeviceDeOutroUsuario(eventoId: UUID, deviceUuid: String, usuarioId: UUID): Boolean =
        jpaRepository.existsByEventoIdAndDeviceUuidAndUsuarioIdNot(eventoId, deviceUuid, usuarioId)

    override fun findByEventoAndUsuario(eventoId: UUID, usuarioId: UUID): List<Presenca> =
        jpaRepository.findByEventoIdAndUsuarioId(eventoId, usuarioId).map { it.toDomain() }

    override fun countByEvento(eventoId: UUID): Long = jpaRepository.countByEventoId(eventoId)
}
