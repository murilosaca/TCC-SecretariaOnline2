package br.ufpr.sept.so2.modules.academico.application.ports

import java.util.UUID

interface CursoSecretarioRepository {
    fun replaceAll(cursoId: UUID, usuarioIds: Collection<UUID>)

    fun adicionarSeAusente(cursoId: UUID, usuarioId: UUID)

    fun findUsuarioIdsByCursoId(cursoId: UUID): List<UUID>

    fun findUsuarioIdsByCursoIds(cursoIds: Collection<UUID>): Map<UUID, List<UUID>>

    fun findCursoIdsByUsuarioId(usuarioId: UUID): Set<UUID>

    fun deleteByCursoId(cursoId: UUID)
}
