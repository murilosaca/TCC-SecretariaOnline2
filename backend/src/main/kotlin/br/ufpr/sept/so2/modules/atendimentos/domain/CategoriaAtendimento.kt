package br.ufpr.sept.so2.modules.atendimentos.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.OffsetDateTime
import java.util.UUID

class CategoriaAtendimento(
    val id: UUID,
    val nome: String,
    val ativo: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    init {
        if (nome.isBlank()) {
            throw DadoInvalidoException("Nome da categoria é obrigatório.")
        }
    }
}
