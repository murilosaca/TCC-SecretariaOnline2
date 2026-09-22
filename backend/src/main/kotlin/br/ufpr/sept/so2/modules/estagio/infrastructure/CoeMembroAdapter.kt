package br.ufpr.sept.so2.modules.estagio.infrastructure

import br.ufpr.sept.so2.modules.estagio.application.ports.CoeMembroPort
import br.ufpr.sept.so2.modules.estagio.infrastructure.persistence.CoeMembroJpaEntity
import br.ufpr.sept.so2.modules.estagio.infrastructure.persistence.CoeMembroJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class CoeMembroAdapter(
    private val jpaRepository: CoeMembroJpaRepository,
) : CoeMembroPort {

    @Transactional(readOnly = true)
    override fun cursosDoMembro(usuarioId: UUID): Set<UUID> =
        jpaRepository.findByIdUsuario(usuarioId).mapNotNull { it.idCurso }.toSet()

    @Transactional(readOnly = true)
    override fun membrosDosCursos(cursoIds: Collection<UUID>): List<UUID> {
        if (cursoIds.isEmpty()) {
            return emptyList()
        }
        return jpaRepository.findByIdCursoIn(cursoIds.toSet())
            .mapNotNull { it.idUsuario }
            .distinct()
    }

    @Transactional(readOnly = true)
    override fun ehMembro(usuarioId: UUID, cursoId: UUID): Boolean =
        jpaRepository.existsByIdCursoAndIdUsuario(cursoId, usuarioId)

    @Transactional
    override fun adicionarSeAusente(cursoId: UUID, usuarioId: UUID) {
        if (jpaRepository.existsByIdCursoAndIdUsuario(cursoId, usuarioId)) {
            return
        }
        jpaRepository.save(
            CoeMembroJpaEntity().apply {
                idCurso = cursoId
                idUsuario = usuarioId
            },
        )
    }
}
