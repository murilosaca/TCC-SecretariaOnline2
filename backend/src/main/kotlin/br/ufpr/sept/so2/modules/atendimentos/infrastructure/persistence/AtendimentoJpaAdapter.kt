package br.ufpr.sept.so2.modules.atendimentos.infrastructure.persistence

import br.ufpr.sept.so2.modules.atendimentos.application.ports.AtendimentoRepository
import br.ufpr.sept.so2.modules.atendimentos.domain.Atendimento
import br.ufpr.sept.so2.modules.atendimentos.domain.AtendimentoEstado
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AtendimentoJpaAdapter(
    private val jpaRepository: AtendimentoJpaRepository,
) : AtendimentoRepository {

    override fun save(atendimento: Atendimento): Atendimento {
        val entity = jpaRepository.findById(atendimento.id)
            .orElseGet { AtendimentoJpaEntity.fromDomain(atendimento) }
        entity.merge(atendimento)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Atendimento? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findByAluno(
        alunoId: UUID,
        estado: AtendimentoEstado?,
        pageable: Pageable,
    ): Page<Atendimento> = jpaRepository.findByAluno(alunoId, estado?.name, pageable).map { it.toDomain() }
}
