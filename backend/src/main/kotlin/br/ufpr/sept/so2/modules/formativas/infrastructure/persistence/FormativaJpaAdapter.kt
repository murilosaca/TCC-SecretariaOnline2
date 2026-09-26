package br.ufpr.sept.so2.modules.formativas.infrastructure.persistence

import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class FormativaJpaAdapter(
    private val jpaRepository: FormativaJpaRepository,
) : FormativaRepository {

    override fun save(formativa: Formativa): Formativa {
        val entity = jpaRepository.findById(formativa.id).orElseGet { FormativaJpaEntity.fromDomain(formativa) }
        entity.merge(formativa)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Formativa? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findByEventoAndAluno(eventoId: UUID, alunoId: UUID): Formativa? =
        jpaRepository.findByIdEventoAndIdAluno(eventoId, alunoId)?.toDomain()

    override fun findByAluno(alunoId: UUID, pageable: Pageable): Page<Formativa> =
        jpaRepository.findByIdAluno(alunoId, pageable).map { it.toDomain() }

    override fun findByEstado(estado: FormativaEstado, pageable: Pageable): Page<Formativa> =
        jpaRepository.findByEstado(estado.name, pageable).map { it.toDomain() }

    override fun somarCargaHoraria(alunoId: UUID, estado: FormativaEstado): Int =
        jpaRepository.somarCargaHoraria(alunoId, estado.name).toInt()

    override fun findPendentesConfirmacao(alunoId: UUID, limite: Int): List<Formativa> =
        jpaRepository.findTop3ByIdAlunoAndEstadoOrderByCreatedAtDesc(
            alunoId,
            FormativaEstado.PENDENTE_CONFIRMACAO.name,
        )
            .take(limite.coerceAtLeast(0))
            .map { it.toDomain() }

    override fun findPoolCaaf(cursoIds: Collection<UUID>, usuarioId: UUID): List<Formativa> {
        if (cursoIds.isEmpty()) {
            return emptyList()
        }
        return jpaRepository.findPoolCaaf(cursoIds.toSet(), usuarioId).map { it.toDomain() }
    }

    override fun countSemResponsavel(cursoIds: Collection<UUID>): Long {
        if (cursoIds.isEmpty()) {
            return 0
        }
        return jpaRepository.countSemResponsavel(cursoIds.toSet())
    }

    override fun countAtribuidasAguardando(cursoIds: Collection<UUID>, responsavelId: UUID): Long {
        if (cursoIds.isEmpty()) {
            return 0
        }
        return jpaRepository.countAtribuidasAguardando(cursoIds.toSet(), responsavelId)
    }

    override fun countAprovadasDesde(cursoIds: Collection<UUID>, inicio: OffsetDateTime): Long {
        if (cursoIds.isEmpty()) {
            return 0
        }
        return jpaRepository.countAprovadasDesde(cursoIds.toSet(), inicio)
    }

    override fun countCargaAguardando(
        cursoIds: Collection<UUID>,
        responsavelIds: Collection<UUID>,
    ): Map<UUID, Long> {
        if (cursoIds.isEmpty() || responsavelIds.isEmpty()) {
            return emptyMap()
        }
        return jpaRepository.countCargaAguardando(cursoIds.toSet(), responsavelIds.toSet())
            .associate { UUID.fromString(it.idResponsavel) to it.total }
    }
}
