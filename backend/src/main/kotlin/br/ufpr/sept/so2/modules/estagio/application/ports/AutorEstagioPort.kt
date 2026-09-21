package br.ufpr.sept.so2.modules.estagio.application.ports

import java.util.UUID

interface AutorEstagioPort {
    fun rotulo(usuarioId: UUID): String?
}
