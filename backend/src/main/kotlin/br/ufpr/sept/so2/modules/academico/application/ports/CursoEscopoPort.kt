package br.ufpr.sept.so2.modules.academico.application.ports

import java.util.UUID

/**
 * Escopo por curso (RF-F5-004-a): secretário N:N ∪ coordenador.
 * Consumido pelo acadêmico e por solicitações (`request.view_curso`).
 * Revalidado no use case — não confiar só em claim JWT.
 */
interface CursoEscopoPort {
    fun cursoIdsDoUsuario(usuarioId: UUID): Set<UUID>

    fun cursoIdDoAlunoPorGrrOuEmail(grr: String?, emailInstitucional: String?): UUID?
}
