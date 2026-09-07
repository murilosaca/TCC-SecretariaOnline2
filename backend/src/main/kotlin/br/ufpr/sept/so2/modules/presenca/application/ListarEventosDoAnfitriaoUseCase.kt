package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListarEventosDoAnfitriaoUseCase(
    private val eventoRepository: EventoRepository,
) {
    @Transactional(readOnly = true)
    fun execute(anfitriaoId: UUID, pageable: Pageable): Page<Evento> {
        val size = pageable.pageSize.coerceIn(1, 100)
        val sort = if (pageable.sort.isSorted) pageable.sort else Sort.by(Sort.Direction.DESC, "inicioEm")
        return eventoRepository.findByAnfitriao(anfitriaoId, PageRequest.of(pageable.pageNumber, size, sort))
    }
}
