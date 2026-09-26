package br.ufpr.sept.so2.modules.solicitacoes.application.ports

import java.util.UUID

data class SolicitanteResumo(
    val nome: String?,
    val grr: String?,
    val cursoId: UUID?,
    val cursoNome: String?,
    val cursoSigla: String?,
)

interface SolicitanteResumoPort {
    fun nomeDe(usuarioId: UUID): String?

    fun resumoDe(usuarioId: UUID): SolicitanteResumo
}
