package br.ufpr.sept.so2.modules.iam.application.ports

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.UUID

data class OutboxEvento(
    val id: UUID,
    val tipo: String,
    val payload: String,
    val status: String,
    val tentativas: Int,
    val createdAt: OffsetDateTime,
    val retriedBy: UUID?,
)

interface OutboxConsultaPort {
    fun listar(status: String?, tipo: String?, pageable: Pageable): Page<OutboxEvento>

    fun reentregar(id: UUID, operadorId: UUID): OutboxEvento

    fun pendingMaisAntigo(): OffsetDateTime?
}
