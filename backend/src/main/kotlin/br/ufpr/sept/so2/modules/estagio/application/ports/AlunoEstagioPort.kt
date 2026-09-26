package br.ufpr.sept.so2.modules.estagio.application.ports

import java.util.UUID

interface AlunoEstagioPort {
    fun resolver(usuarioId: UUID): AlunoRef?

    fun nomeDe(alunoId: UUID): String?

    fun cadastro(alunoId: UUID): Cadastro?

    data class AlunoRef(
        val id: UUID,
        val egresso: Boolean,
    )

    data class Cadastro(
        val id: UUID,
        val idCurso: UUID,
    )
}
