package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CursoSecretarioJpaAdapter(
    private val jpaRepository: CursoSecretarioJpaRepository,
    private val entityManager: EntityManager,
) : CursoSecretarioRepository {
    override fun replaceAll(cursoId: UUID, usuarioIds: Collection<UUID>) {
        jpaRepository.deleteByIdCurso(cursoId)
        entityManager.flush()
        val distintos = usuarioIds.toSet()
        if (distintos.isEmpty()) {
            return
        }
        jpaRepository.saveAll(
            distintos.map { usuarioId ->
                CursoSecretarioJpaEntity().apply {
                    idCurso = cursoId
                    idUsuario = usuarioId
                }
            },
        )
    }

    override fun findUsuarioIdsByCursoId(cursoId: UUID): List<UUID> =
        jpaRepository.findByIdCurso(cursoId).mapNotNull { it.idUsuario }

    override fun findUsuarioIdsByCursoIds(cursoIds: Collection<UUID>): Map<UUID, List<UUID>> {
        if (cursoIds.isEmpty()) {
            return emptyMap()
        }
        return jpaRepository.findByIdCursoIn(cursoIds.toSet())
            .groupBy({ it.idCurso!! }, { it.idUsuario!! })
    }

    override fun findCursoIdsByUsuarioId(usuarioId: UUID): Set<UUID> =
        jpaRepository.findByIdUsuario(usuarioId).mapNotNull { it.idCurso }.toSet()

    override fun deleteByCursoId(cursoId: UUID) {
        jpaRepository.deleteByIdCurso(cursoId)
    }
}
