package br.ufpr.sept.so2.modules.tcc.application.ports

import java.util.UUID

interface AutorTccPort {
    fun rotulo(usuarioId: UUID): String?
}
