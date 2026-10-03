package br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence

import br.ufpr.sept.so2.modules.comunicacao.application.ports.ComunicacaoRepository
import br.ufpr.sept.so2.modules.comunicacao.domain.Comunicacao
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ComunicacaoJpaAdapter(
    private val jpaRepository: ComunicacaoJpaRepository,
) : ComunicacaoRepository {
    override fun save(comunicacao: Comunicacao): Comunicacao {
        val entity = jpaRepository.findById(comunicacao.id).orElseGet { ComunicacaoJpaEntity.fromDomain(comunicacao) }
        entity.tipo = comunicacao.tipo.name
        entity.titulo = comunicacao.titulo
        entity.corpo = comunicacao.corpo
        entity.prioridade = comunicacao.prioridade.name
        entity.autorId = comunicacao.autorId
        entity.audienciaTipo = comunicacao.audienciaTipo.name
        entity.audienciaId = comunicacao.audienciaId
        entity.expiresAt = comunicacao.expiresAt
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Comunicacao? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findByAutor(autorId: UUID): List<Comunicacao> =
        jpaRepository.findByAutorId(autorId).map { it.toDomain() }

    override fun findByIds(ids: Collection<UUID>): List<Comunicacao> {
        if (ids.isEmpty()) {
            return emptyList()
        }
        return jpaRepository.findAllById(ids).map { it.toDomain() }
    }

    override fun findByTitulo(titulo: String): Comunicacao? =
        jpaRepository.findByTitulo(titulo).map { it.toDomain() }.orElse(null)
}
