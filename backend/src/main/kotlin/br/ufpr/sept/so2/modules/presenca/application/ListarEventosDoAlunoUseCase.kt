package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class ListarEventosDoAlunoUseCase(
    private val eventoRepository: EventoRepository,
) {
    @Transactional(readOnly = true)
    fun execute(audience: String, pageable: Pageable): Page<Evento> {
        if (!audience.equals("me", ignoreCase = true)) {
            throw AcessoNegadoException("Neste momento só é possível listar os eventos disponíveis para você.")
        }
        val size = pageable.pageSize.coerceIn(1, 100)
        val sort = if (pageable.sort.isSorted) pageable.sort else Sort.by(Sort.Direction.DESC, "inicioEm")
        return eventoRepository.findAbertosParaAluno(
            OffsetDateTime.now(),
            PageRequest.of(pageable.pageNumber, size, sort),
        )
    }
}
