package br.ufpr.sept.so2.modules.tcc.application.ports

import java.util.UUID

interface AlunoTccPort {
    fun resolver(usuarioId: UUID): AlunoRef?

    fun nomeDe(alunoId: UUID): String?

    data class AlunoRef(val id: UUID, val egresso: Boolean)
}
