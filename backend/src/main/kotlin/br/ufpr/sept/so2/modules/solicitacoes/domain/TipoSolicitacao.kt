package br.ufpr.sept.so2.modules.solicitacoes.domain

import java.time.OffsetDateTime
import java.util.UUID

class TipoSolicitacao(
    val id: UUID,
    val codigo: String,
    val nome: String,
    val descricao: String?,
    val status: String,
    val formSchema: String,
    val workflowJson: String,
    val prazoDias: Int,
    val versao: Int,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
) {
    fun isPublished(): Boolean = PUBLISHED == status

    companion object {
        const val DRAFT = "DRAFT"
        const val PUBLISHED = "PUBLISHED"
    }
}
