package br.ufpr.sept.so2.modules.formativas.application.ports

import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.UUID

interface FormativaRepository {
    fun save(formativa: Formativa): Formativa

    fun findById(id: UUID): Formativa?

    fun findByEventoAndAluno(eventoId: UUID, alunoId: UUID): Formativa?

    fun findByAluno(alunoId: UUID, pageable: Pageable): Page<Formativa>

    fun findByEstado(estado: FormativaEstado, pageable: Pageable): Page<Formativa>

    fun somarCargaHoraria(alunoId: UUID, estado: FormativaEstado): Int

    fun findPendentesConfirmacao(alunoId: UUID, limite: Int): List<Formativa>

    fun findPoolCaaf(cursoIds: Collection<UUID>, usuarioId: UUID): List<Formativa>

    fun countSemResponsavel(cursoIds: Collection<UUID>): Long

    fun countAtribuidasAguardando(cursoIds: Collection<UUID>, responsavelId: UUID): Long

    fun countAprovadasDesde(cursoIds: Collection<UUID>, inicio: OffsetDateTime): Long

    fun countCargaAguardando(cursoIds: Collection<UUID>, responsavelIds: Collection<UUID>): Map<UUID, Long>
}
