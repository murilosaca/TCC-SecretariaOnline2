package br.ufpr.sept.so2.modules.atendimentos.infrastructure.persistence

import br.ufpr.sept.so2.modules.atendimentos.application.ports.CategoriaAtendimentoRepository
import br.ufpr.sept.so2.modules.atendimentos.domain.CategoriaAtendimento
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CategoriaAtendimentoJpaAdapter(
    private val jpaRepository: CategoriaAtendimentoJpaRepository,
) : CategoriaAtendimentoRepository {

    override fun save(categoria: CategoriaAtendimento): CategoriaAtendimento {
        val entity = jpaRepository.findById(categoria.id)
            .orElseGet { CategoriaAtendimentoJpaEntity.fromDomain(categoria) }
        entity.merge(categoria)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): CategoriaAtendimento? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findByNome(nome: String): CategoriaAtendimento? =
        jpaRepository.findByNomeIgnoreCase(nome).map { it.toDomain() }.orElse(null)

    override fun findAtivas(): List<CategoriaAtendimento> =
        jpaRepository.findByAtivoTrueOrderByNomeAsc().map { it.toDomain() }
}
