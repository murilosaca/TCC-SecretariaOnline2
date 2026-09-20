package br.ufpr.sept.so2.modules.certificados.application.ports

import java.util.UUID

interface BeneficiarioPort {
    fun resolverPorUsuario(usuarioId: UUID): BeneficiarioRef?

    fun nomeDe(alunoId: UUID): String?

    data class BeneficiarioRef(
        val id: UUID,
        val nome: String,
        val egresso: Boolean,
    )
}
