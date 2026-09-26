package br.ufpr.sept.so2.modules.formativas.application.ports

import java.util.UUID

interface AutorFormativaPort {
    fun rotulo(usuarioId: UUID): String?
}
