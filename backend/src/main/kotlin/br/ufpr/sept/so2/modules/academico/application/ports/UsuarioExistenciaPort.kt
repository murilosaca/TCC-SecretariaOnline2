package br.ufpr.sept.so2.modules.academico.application.ports

import java.util.UUID

/** Valida existência de usuários IAM antes de gravar vínculos (coordenador / secretários). */
interface UsuarioExistenciaPort {
    fun existe(id: UUID): Boolean

    fun existemTodos(ids: Collection<UUID>): Boolean
}
