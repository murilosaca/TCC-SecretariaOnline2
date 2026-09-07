package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.modules.iam.application.ports.SenhaHistoricoRepository
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class SenhaHistoricoJpaAdapter(
    private val jpaRepository: SenhaHistoricoJpaRepository,
) : SenhaHistoricoRepository {
    override fun append(usuarioId: UUID, senhaHash: String) {
        jpaRepository.save(SenhaHistoricoJpaEntity(Uuids.v7(), usuarioId, senhaHash, OffsetDateTime.now()))
    }

    override fun findLastHashes(usuarioId: UUID, limite: Int): List<String> =
        jpaRepository.findTop3ByUsuarioIdOrderByCreatedAtDesc(usuarioId)
            .asSequence()
            .take(limite)
            .map { it.senhaHash!! }
            .toList()
}
