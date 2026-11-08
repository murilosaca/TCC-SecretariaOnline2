package br.ufpr.sept.so2.modules.comunicacao.application.ports

import java.util.UUID

fun interface EmailUsuarioPort {
    fun emailDe(usuarioId: UUID): String?
}
