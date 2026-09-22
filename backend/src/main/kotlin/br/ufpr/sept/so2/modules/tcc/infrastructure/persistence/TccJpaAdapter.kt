package br.ufpr.sept.so2.modules.tcc.infrastructure.persistence

import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import br.ufpr.sept.so2.modules.tcc.domain.TccSituacao
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class TccJpaAdapter(
    private val jpaRepository: TccJpaRepository,
) : TccRepository {

    @Transactional
    override fun save(tcc: Tcc): Tcc {
        val entity = jpaRepository.findById(tcc.id).orElseGet { TccJpaEntity.fromDomain(tcc) }
        entity.merge(tcc)
        return jpaRepository.saveAndFlush(entity).toDomain()
    }

    @Transactional(readOnly = true)
    override fun findById(id: UUID): Tcc? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    @Transactional(readOnly = true)
    override fun findByAluno(alunoId: UUID, estado: TccEstado?, pageable: Pageable): Page<Tcc> {
        val page = if (estado == null) {
            jpaRepository.findByIdAluno(alunoId, pageable)
        } else {
            jpaRepository.findByIdAlunoAndEstado(alunoId, estado.name, pageable)
        }
        return page.map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun findParaRevisao(usuarioId: UUID, estado: TccEstado?, pageable: Pageable): Page<Tcc> {
        val filtro = estado ?: TccEstado.SUBMETIDO
        return jpaRepository.findParaRevisao(usuarioId, filtro.name, pageable).map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun existsAtivoByAluno(alunoId: UUID): Boolean =
        jpaRepository.existsByIdAlunoAndSituacao(alunoId, TccSituacao.ATIVO.name)
}
