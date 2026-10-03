package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.application.ports.OutboxClaim
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxConsultaPort
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxEvento
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.util.UUID

@Component
class OutboxJpaAdapter(
    private val jpaRepository: OutboxEventJpaRepository,
    private val entityManager: EntityManager,
    @Value("\${spring.datasource.url}") private val jdbcUrl: String,
) : OutboxPort, OutboxConsultaPort {

    override fun enqueue(tipo: String, payload: String) {
        jpaRepository.save(OutboxEventJpaEntity(tipo, payload))
    }

    /**
     * Claim atômico. PostgreSQL usa `FOR UPDATE SKIP LOCKED`. H2 (ITs) não tem SKIP LOCKED:
     * a query equivalente sem a cláusula basta — os testes não disputam o lote.
     */
    @Transactional
    override fun claimPending(limit: Int, staleBefore: OffsetDateTime): List<OutboxClaim> {
        if (limit <= 0) {
            return emptyList()
        }
        val sql = if (isPostgres()) {
            SQL_POSTGRES
        } else {
            SQL_H2
        }
        val query = entityManager.createNativeQuery(sql, OutboxEventJpaEntity::class.java)
        query.setParameter("stale", Timestamp.from(staleBefore.toInstant()))
        query.setParameter("limite", limit)
        @Suppress("UNCHECKED_CAST")
        val linhas = query.resultList as List<OutboxEventJpaEntity>
        linhas.forEach { it.marcarProcessamento() }
        entityManager.flush()
        return linhas.map { entidade ->
            OutboxClaim(
                entidade.id!!,
                entidade.tipo!!,
                entidade.payload!!,
                entidade.tentativas,
            )
        }
    }

    @Transactional
    override fun markSent(id: UUID) {
        jpaRepository.findById(id).ifPresent { entidade ->
            entidade.marcarEnviado()
            jpaRepository.save(entidade)
        }
    }

    @Transactional
    override fun markFailed(id: UUID, tentativas: Int, lastError: String?) {
        jpaRepository.findById(id).ifPresent { entidade ->
            entidade.marcarFalha(tentativas, lastError)
            jpaRepository.save(entidade)
        }
    }

    @Transactional
    override fun markDead(id: UUID, tentativas: Int, lastError: String?) {
        jpaRepository.findById(id).ifPresent { entidade ->
            entidade.marcarEsgotado(tentativas, lastError)
            jpaRepository.save(entidade)
        }
    }

    @Transactional
    override fun markPendingRetry(id: UUID, tentativas: Int, lastError: String?) {
        jpaRepository.findById(id).ifPresent { entidade ->
            entidade.marcarRetry(tentativas, lastError)
            jpaRepository.save(entidade)
        }
    }

    override fun listar(status: String?, tipo: String?, pageable: Pageable): Page<OutboxEvento> {
        val size = pageable.pageSize.coerceIn(1, 100)
        val numero = pageable.pageNumber.coerceAtLeast(0)
        val pagina = PageRequest.of(numero, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        return jpaRepository.filtrar(status, tipo, pagina).map { it.toEvento() }
    }

    @Transactional
    override fun reentregar(id: UUID, operadorId: UUID): OutboxEvento {
        val entidade = jpaRepository.findByIdForUpdate(id)
            .orElseThrow { RecursoNaoEncontradoException("Evento de outbox não encontrado.") }
        val status = entidade.status
        if (status != OutboxEventJpaEntity.STATUS_FAILED && status != OutboxEventJpaEntity.STATUS_DEAD) {
            throw ConflitoEstadoException("Evento $status não pode ser reentregue.")
        }
        entidade.reentregar(operadorId)
        return jpaRepository.save(entidade).toEvento()
    }

    override fun pendingMaisAntigo(): OffsetDateTime? = jpaRepository.menorPending()

    private fun OutboxEventJpaEntity.toEvento(): OutboxEvento =
        OutboxEvento(
            id!!,
            tipo!!,
            payload!!,
            status!!,
            tentativas,
            createdAt!!,
            retriedBy,
        )

    private fun isPostgres(): Boolean = jdbcUrl.startsWith("jdbc:postgresql")

    companion object {
        private const val WHERE_CLAIM = """
            FROM outbox_event
            WHERE status = 'PENDING'
               OR (status = 'PROCESSING' AND updated_at < :stale)
            ORDER BY created_at ASC
            LIMIT :limite
            """

        private const val SQL_POSTGRES = "SELECT * $WHERE_CLAIM FOR UPDATE SKIP LOCKED"

        private const val SQL_H2 = "SELECT * $WHERE_CLAIM"
    }
}
