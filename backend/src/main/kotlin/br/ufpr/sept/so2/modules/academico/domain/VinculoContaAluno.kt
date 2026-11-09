package br.ufpr.sept.so2.modules.academico.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import java.util.UUID

/**
 * F5.6-D02: não duplica usuário. Vincula se o par GRR + e-mail institucional
 * já é o mesmo usuário. Qualquer outro cruzamento é 409.
 */
data class ContaExistente(
    val id: UUID,
    val grr: String?,
    val emailInstitucional: String,
    val ativo: Boolean,
)

object VinculoContaAluno {
    const val MSG_INCOMPATIVEL =
        "Já existe usuário com GRR ou e-mail incompatível com este aluno."

    /**
     * @return id para vincular, ou null quando a conta ainda não existe.
     */
    fun resolver(porGrr: ContaExistente?, porEmail: ContaExistente?, grr: String, email: String): UUID? {
        if (porGrr == null && porEmail == null) {
            return null
        }
        if (porGrr != null && porEmail != null && porGrr.id != porEmail.id) {
            throw ConflitoEstadoException(MSG_INCOMPATIVEL)
        }
        val conta = porGrr ?: porEmail!!
        val grrOk = conta.grr?.equals(grr, ignoreCase = true) == true
        val emailOk = conta.emailInstitucional.equals(email, ignoreCase = true)
        if (!conta.ativo || !grrOk || !emailOk) {
            throw ConflitoEstadoException(MSG_INCOMPATIVEL)
        }
        return conta.id
    }
}
