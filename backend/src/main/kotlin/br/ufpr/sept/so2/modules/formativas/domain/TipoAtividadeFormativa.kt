package br.ufpr.sept.so2.modules.formativas.domain

import java.time.OffsetDateTime
import java.util.UUID

class TipoAtividadeFormativa(
    val id: UUID,
    val cursoId: UUID,
    val nome: String,
    val ativo: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
