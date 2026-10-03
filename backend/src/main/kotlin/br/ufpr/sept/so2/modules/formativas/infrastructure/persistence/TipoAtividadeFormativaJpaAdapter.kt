package br.ufpr.sept.so2.modules.formativas.infrastructure.persistence

import br.ufpr.sept.so2.modules.formativas.application.ports.TipoAtividadeFormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.TipoAtividadeFormativa
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TipoAtividadeFormativaJpaAdapter(
    private val jpaRepository: TipoAtividadeFormativaJpaRepository,
) : TipoAtividadeFormativaRepository {
    override fun save(tipo: TipoAtividadeFormativa): TipoAtividadeFormativa {
        val entity = jpaRepository.findById(tipo.id).orElseGet { TipoAtividadeFormativaJpaEntity.fromDomain(tipo) }
        entity.cursoId = tipo.cursoId
        entity.nome = tipo.nome
        entity.ativo = tipo.ativo
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): TipoAtividadeFormativa? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findAtivosDoCurso(cursoId: UUID): List<TipoAtividadeFormativa> =
        jpaRepository.findByCursoIdAndAtivoTrueOrderByNomeAsc(cursoId).map { it.toDomain() }

    override fun findByCursoENome(cursoId: UUID, nome: String): TipoAtividadeFormativa? =
        jpaRepository.findByCursoIdAndNome(cursoId, nome).map { it.toDomain() }.orElse(null)
}
