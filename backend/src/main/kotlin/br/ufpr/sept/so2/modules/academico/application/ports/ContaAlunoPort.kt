package br.ufpr.sept.so2.modules.academico.application.ports

import java.util.UUID

/**
 * Porta do IAM para a conta do aluno.
 * A secretaria não recebe a senha e não ganha user.manage_all nem user.reset_password.
 */
interface ContaAlunoPort {
    fun provisionar(comando: ProvisionarContaAluno): ContaProvisionada

    /** Conta já compatível, ou null se não houver. 409 se o par GRR/e-mail divergir. */
    fun localizar(grr: String, emailInstitucional: String): UUID?

    fun resetarSenha(operadorId: UUID, usuarioId: UUID, ip: String?)
}

data class ProvisionarContaAluno(
    val operadorId: UUID,
    val nome: String,
    val grr: String,
    val emailInstitucional: String,
    val emailPessoal: String?,
    val ip: String?,
)

data class ContaProvisionada(
    val usuarioId: UUID,
    val vinculada: Boolean,
)
