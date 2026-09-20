package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListarFilaRevisaoFormativaUseCase(
    private val formativaRepository: FormativaRepository,
) {
    @Transactional(readOnly = true)
    fun execute(pageable: Pageable): Page<Formativa> {
        val size = pageable.pageSize.coerceIn(1, 100)
        val sort = if (pageable.sort.isSorted) {
            pageable.sort
        } else {
            Sort.by(Sort.Direction.ASC, "createdAt")
        }
        return formativaRepository.findByEstado(
            FormativaEstado.AGUARDANDO_CAAF,
            PageRequest.of(pageable.pageNumber, size, sort),
        )
    }
}
