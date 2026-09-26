package br.ufpr.sept.so2.modules.bff.application.ports

import java.util.UUID

fun interface ProfessorIdentidadeQueryPort {
    fun consultar(usuarioId: UUID): ProfessorIdentidade

    data class ProfessorIdentidade(val nome: String)
}
