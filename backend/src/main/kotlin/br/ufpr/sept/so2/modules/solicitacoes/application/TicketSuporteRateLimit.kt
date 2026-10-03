package br.ufpr.sept.so2.modules.solicitacoes.application

import br.ufpr.sept.so2.shared.domain.exception.RateLimitExcedidoException
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.ArrayDeque
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.max

/**
 * Três tickets de suporte por hora por usuário, em memória, no mesmo
 * desenho do filtro de autenticação. Rejeita antes de abrir a transação.
 */
@Component
class TicketSuporteRateLimit {
    private val janelas = ConcurrentHashMap<UUID, ArrayDeque<Long>>()

    fun verificar(userId: UUID) {
        val retry = registrar(userId)
        if (retry > 0) {
            throw RateLimitExcedidoException(retry)
        }
    }

    private fun registrar(userId: UUID): Int {
        val agora = Instant.now().toEpochMilli()
        val inicio = agora - JANELA_MS
        val hits = janelas.computeIfAbsent(userId) { ArrayDeque() }
        synchronized(hits) {
            while (hits.isNotEmpty() && hits.peekFirst() < inicio) {
                hits.removeFirst()
            }
            if (hits.size >= LIMITE) {
                val maisAntigo = hits.peekFirst()
                return max(1, ceil((maisAntigo + JANELA_MS - agora) / 1000.0).toInt())
            }
            hits.addLast(agora)
            return 0
        }
    }

    companion object {
        private const val LIMITE = 3
        private const val JANELA_MS = 3_600_000L
    }
}
