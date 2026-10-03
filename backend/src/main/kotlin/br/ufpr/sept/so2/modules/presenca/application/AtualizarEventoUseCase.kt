package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AtualizarEventoUseCase(
    private val eventoRepository: EventoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
) {
    @Transactional
    fun execute(
        id: UUID,
        atorId: UUID,
        authorities: List<String>,
        titulo: String?,
        inicioEm: OffsetDateTime?,
        fimEm: OffsetDateTime?,
        cargaHoraria: Int?,
        attendanceModeRaw: String?,
        cursoId: UUID?,
    ): Evento {
        val evento = EventoEdicao.carregar(id, eventoRepository, atorId, authorities, cursoEscopoPort)
        evento.atualizar(
            titulo,
            inicioEm,
            fimEm,
            cargaHoraria,
            attendanceModeRaw?.takeIf { it.isNotBlank() }?.let { AttendanceMode.from(it) },
            EventoEdicao.cursoDestino(cursoId, atorId, authorities, cursoEscopoPort),
            OffsetDateTime.now(),
        )
        return eventoRepository.save(evento)
    }
}

/**
 * Evento fora do alcance do ator responde 404 (anti-enumeração), igual ao
 * recorte de diploma; curso explícito fora do escopo responde 403.
 */
internal object EventoEdicao {
    const val NAO_ENCONTRADO = "Evento não encontrado."

    fun carregar(
        id: UUID,
        repository: EventoRepository,
        atorId: UUID,
        authorities: List<String>,
        cursoEscopoPort: CursoEscopoPort,
    ): Evento {
        val evento = repository.findById(id).orElseThrow { RecursoNaoEncontradoException(NAO_ENCONTRADO) }
        val alcanca = if (EventoAcesso.VIEW_CURSO in authorities) {
            evento.idCurso in EventoAcesso.cursos(cursoEscopoPort, atorId)
        } else {
            evento.eAnfitriao(atorId)
        }
        if (!alcanca) {
            throw RecursoNaoEncontradoException(NAO_ENCONTRADO)
        }
        return evento
    }

    fun cursoDestino(
        cursoId: UUID?,
        atorId: UUID,
        authorities: List<String>,
        cursoEscopoPort: CursoEscopoPort,
    ): UUID? {
        if (cursoId == null || EventoAcesso.VIEW_CURSO !in authorities) {
            return null
        }
        return EventoAcesso.exigirCursoNoEscopo(cursoId, EventoAcesso.cursos(cursoEscopoPort, atorId))
    }
}
