package br.ufpr.sept.so2.modules.bff.application.ports

import java.util.UUID

fun interface AlunoIdentidadeQueryPort {
    fun consultar(usuarioId: UUID): AlunoIdentidade

    data class AlunoIdentidade(val nome: String, val curso: String?)
}
