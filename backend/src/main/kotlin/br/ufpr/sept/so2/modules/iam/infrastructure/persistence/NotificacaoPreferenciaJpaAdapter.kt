package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.application.ports.NotificacaoPreferenciaRepository
import br.ufpr.sept.so2.modules.iam.domain.CanalNotificacao
import br.ufpr.sept.so2.modules.iam.domain.ModoDigest
import br.ufpr.sept.so2.modules.iam.domain.NotificacaoPreferencia
import br.ufpr.sept.so2.modules.iam.domain.PrioridadeNotificacao
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class NotificacaoPreferenciaJpaAdapter(
    private val jpaRepository: NotificacaoPreferenciaJpaRepository,
    private val objectMapper: ObjectMapper,
) : NotificacaoPreferenciaRepository {
    override fun findByUsuarioId(usuarioId: UUID): Optional<NotificacaoPreferencia> =
        jpaRepository.findById(usuarioId).map { toDomain(it) }

    override fun save(preferencia: NotificacaoPreferencia): NotificacaoPreferencia {
        val entity = jpaRepository.findById(preferencia.usuarioId).orElseGet { NotificacaoPreferenciaJpaEntity() }
        entity.usuarioId = preferencia.usuarioId
        entity.canais = objectMapper.writeValueAsString(serializar(preferencia.canais))
        entity.dndInicio = preferencia.dndInicio
        entity.dndFim = preferencia.dndFim
        entity.digest = preferencia.digest.name
        entity.createdAt = preferencia.createdAt
        entity.updatedAt = preferencia.updatedAt
        return toDomain(jpaRepository.save(entity))
    }

    private fun toDomain(entity: NotificacaoPreferenciaJpaEntity): NotificacaoPreferencia =
        NotificacaoPreferencia(
            entity.usuarioId!!,
            desserializar(entity.canais),
            entity.dndInicio,
            entity.dndFim,
            ModoDigest.valueOf(entity.digest!!),
            entity.createdAt!!,
            entity.updatedAt!!,
        )

    private fun serializar(
        canais: Map<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>>,
    ): Map<String, Map<String, Boolean>> =
        canais.mapKeys { it.key.name }.mapValues { prioridade ->
            prioridade.value.mapKeys { canal -> if (canal.key == CanalNotificacao.IN_APP) "inApp" else "email" }
        }

    private fun desserializar(json: String?): Map<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>> {
        if (json.isNullOrBlank()) {
            return PrioridadeNotificacao.entries.associateWith {
                CanalNotificacao.entries.associateWith { true }
            }
        }
        val bruto = objectMapper.readValue(json, MAPA)
        val resultado = linkedMapOf<PrioridadeNotificacao, Map<CanalNotificacao, Boolean>>()
        for (prioridade in PrioridadeNotificacao.entries) {
            val canais = bruto[prioridade.name].orEmpty()
            resultado[prioridade] = CanalNotificacao.entries.associateWith { canal ->
                val chave = if (canal == CanalNotificacao.IN_APP) "inApp" else "email"
                canais[chave] ?: true
            }
        }
        return resultado
    }

    companion object {
        private val MAPA = object : TypeReference<Map<String, Map<String, Boolean>>>() {}
    }
}
