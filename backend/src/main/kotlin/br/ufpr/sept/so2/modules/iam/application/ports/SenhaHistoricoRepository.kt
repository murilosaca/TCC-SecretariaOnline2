package br.ufpr.sept.so2.modules.iam.application.ports

import java.util.UUID

interface SenhaHistoricoRepository {
    fun append(usuarioId: UUID, senhaHash: String)

    fun findLastHashes(usuarioId: UUID, limite: Int): List<String>
}
