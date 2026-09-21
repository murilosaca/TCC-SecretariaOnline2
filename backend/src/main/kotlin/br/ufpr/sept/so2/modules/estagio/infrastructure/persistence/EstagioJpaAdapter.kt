package br.ufpr.sept.so2.modules.estagio.infrastructure.persistence

import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.EstagioSituacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class EstagioJpaAdapter(
    private val jpaRepository: EstagioJpaRepository,
) : EstagioRepository {

    @Transactional
    override fun save(estagio: Estagio): Estagio {
        val entity = jpaRepository.findById(estagio.id).orElseGet { EstagioJpaEntity.fromDomain(estagio) }
        entity.merge(estagio)
        return jpaRepository.saveAndFlush(entity).toDomain()
    }

    @Transactional(readOnly = true)
    override fun findById(id: UUID): Estagio? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    @Transactional(readOnly = true)
    override fun findByAluno(alunoId: UUID, situacao: EstagioSituacao?, pageable: Pageable): Page<Estagio> {
        val page = if (situacao == null) {
            jpaRepository.findByIdAluno(alunoId, pageable)
        } else {
            jpaRepository.findByIdAlunoAndSituacao(alunoId, situacao.name, pageable)
        }
        return page.map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun findParaRevisao(
        orientadorId: UUID,
        situacao: EstagioSituacao?,
        pageable: Pageable,
    ): Page<Estagio> {
        val page = if (situacao == null) {
            jpaRepository.findByIdOrientadorAndSituacaoIn(
                orientadorId,
                listOf(EstagioSituacao.ATIVO.name, EstagioSituacao.PENDENTE.name),
                pageable,
            )
        } else {
            jpaRepository.findByIdOrientadorAndSituacao(orientadorId, situacao.name, pageable)
        }
        return page.map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun existsByAluno(alunoId: UUID): Boolean = jpaRepository.existsByIdAluno(alunoId)
}
