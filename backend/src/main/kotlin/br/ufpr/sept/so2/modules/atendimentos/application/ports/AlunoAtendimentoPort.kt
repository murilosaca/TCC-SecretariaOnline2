package br.ufpr.sept.so2.modules.atendimentos.application.ports

import java.util.UUID

/**
 * Ponte para o cadastro acadêmico: o atendimento é registrado para um aluno do
 * escopo da secretaria e lido pelo aluno autenticado.
 */
interface AlunoAtendimentoPort {
    fun cadastro(alunoId: UUID): Cadastro?

    fun resolverPorUsuario(usuarioId: UUID): Cadastro?

    data class Cadastro(
        val id: UUID,
        val nome: String,
        val grr: String?,
        val idCurso: UUID,
    )
}
