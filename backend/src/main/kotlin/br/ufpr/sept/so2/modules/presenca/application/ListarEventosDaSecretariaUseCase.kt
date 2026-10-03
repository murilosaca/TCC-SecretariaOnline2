package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/** Eventos dos cursos da secretaria (F5.14), sobre o mesmo motor `/events`. */
@Service
class ListarEventosDaSecretariaUseCase(
    private val eventoRepository: EventoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
) {
    @Transactional(readOnly = true)
    fun execute(atorId: UUID, cursoId: UUID?, estadoRaw: String?, pageable: Pageable): Page<Evento> {
        val escopo = EventoAcesso.cursos(cursoEscopoPort, atorId)
        val cursos = if (cursoId == null) escopo else setOf(EventoAcesso.exigirCursoNoEscopo(cursoId, escopo))
        val estado = estadoRaw?.takeIf { it.isNotBlank() }?.let { EventoEstado.from(it).name }
        return eventoRepository.findByCursos(cursos, estado, paginar(pageable))
    }

    private fun paginar(pageable: Pageable): Pageable = PageRequest.of(
        pageable.pageNumber,
        pageable.pageSize.coerceAtMost(MAX_SIZE),
        Sort.by(Sort.Direction.DESC, "inicioEm"),
    )

    companion object {
        private const val MAX_SIZE = 100
    }
}
