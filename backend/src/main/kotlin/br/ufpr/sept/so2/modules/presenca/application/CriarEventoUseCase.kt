package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CriarEventoUseCase(
    private val eventoRepository: EventoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
) {
    /**
     * Mesmo `POST /events` para professor e secretaria. Quem tem
     * `event.view_curso` precisa informar um curso do próprio escopo; o
     * professor segue criando evento sem curso.
     */
    @Transactional
    fun execute(
        anfitriaoId: UUID,
        titulo: String,
        inicioEm: OffsetDateTime,
        fimEm: OffsetDateTime,
        cargaHoraria: Int,
        attendanceModeRaw: String?,
        cursoId: UUID? = null,
        authorities: List<String> = emptyList(),
    ): Evento = eventoRepository.save(
        Evento.criar(
            Uuids.v7(),
            anfitriaoId,
            titulo,
            inicioEm,
            fimEm,
            cargaHoraria,
            AttendanceMode.from(attendanceModeRaw ?: AttendanceMode.SECRET_SINGLE.name),
            OffsetDateTime.now(),
            curso(anfitriaoId, cursoId, authorities),
        ),
    )

    private fun curso(anfitriaoId: UUID, cursoId: UUID?, authorities: List<String>): UUID? {
        if (EventoAcesso.VIEW_CURSO !in authorities) {
            return null
        }
        return EventoAcesso.exigirCursoNoEscopo(cursoId, EventoAcesso.cursos(cursoEscopoPort, anfitriaoId))
    }
}
