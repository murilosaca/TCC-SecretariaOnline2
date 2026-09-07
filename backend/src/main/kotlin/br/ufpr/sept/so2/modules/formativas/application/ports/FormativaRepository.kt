package br.ufpr.sept.so2.modules.formativas.application.ports

import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface FormativaRepository {
    fun save(formativa: Formativa): Formativa

    fun findById(id: UUID): Formativa?

    fun findByEventoAndAluno(eventoId: UUID, alunoId: UUID): Formativa?

    fun findByAluno(alunoId: UUID, pageable: Pageable): Page<Formativa>

    fun somarCargaHoraria(alunoId: UUID, estado: FormativaEstado): Int

    fun findPendentesConfirmacao(alunoId: UUID, limite: Int): List<Formativa>
}
