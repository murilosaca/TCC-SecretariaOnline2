package br.ufpr.sept.so2.modules.estagio.application.ports

import java.util.UUID

interface CoeMembroPort {
    fun cursosDoMembro(usuarioId: UUID): Set<UUID>

    fun membrosDosCursos(cursoIds: Collection<UUID>): List<UUID>

    fun ehMembro(usuarioId: UUID, cursoId: UUID): Boolean

    fun adicionarSeAusente(cursoId: UUID, usuarioId: UUID)
}
