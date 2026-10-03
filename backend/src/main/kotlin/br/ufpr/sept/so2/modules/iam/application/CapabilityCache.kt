package br.ufpr.sept.so2.modules.iam.application

import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache local das capabilities materializadas. Evict no login (o JWT novo
 * já traz a união). Put depois do COMMIT de perfil ou matriz.
 */
@Component
class CapabilityCache {
    private val dados = ConcurrentHashMap<UUID, List<String>>()

    fun put(usuarioId: UUID, authorities: List<String>) {
        dados[usuarioId] = authorities.toList()
    }

    fun evict(usuarioId: UUID) {
        dados.remove(usuarioId)
    }

    fun atual(usuarioId: UUID): List<String>? = dados[usuarioId]
}
