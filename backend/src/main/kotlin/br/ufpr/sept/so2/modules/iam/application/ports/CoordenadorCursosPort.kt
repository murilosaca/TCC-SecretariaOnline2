package br.ufpr.sept.so2.modules.iam.application.ports

import java.util.UUID

interface CoordenadorCursosPort {
    fun ids(usuarioId: UUID): Set<UUID>
}
