package br.ufpr.sept.so2.modules.presenca.application

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CriarEventoUseCase(
    private val eventoRepository: EventoRepository,
    private val cursoEscopoPort: CursoEscopoPort,
    private val cursoRepository: CursoRepository,
) {
    /**
     * Mesmo `POST /events` para professor e secretaria. Quem tem
     * `event.view_curso` informa um curso do próprio escopo. O professor
     * informa um curso ativo como público-alvo. A janela pré-agendada é
     * opcional: sem ela, a abertura ao vivo continua valendo 15 minutos.
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
        janelaEntradaInicio: OffsetDateTime? = null,
        janelaEntradaFim: OffsetDateTime? = null,
        janelaSaidaInicio: OffsetDateTime? = null,
        janelaSaidaFim: OffsetDateTime? = null,
    ): Evento {
        val evento = Evento.criar(
            Uuids.v7(),
            anfitriaoId,
            titulo,
            inicioEm,
            fimEm,
            cargaHoraria,
            AttendanceMode.from(attendanceModeRaw ?: AttendanceMode.SECRET_SINGLE.name),
            OffsetDateTime.now(),
            curso(anfitriaoId, cursoId, authorities),
        )
        evento.agendarJanelas(janelaEntradaInicio, janelaEntradaFim, janelaSaidaInicio, janelaSaidaFim)
        return eventoRepository.save(evento)
    }

    private fun curso(anfitriaoId: UUID, cursoId: UUID?, authorities: List<String>): UUID? {
        if (EventoAcesso.VIEW_CURSO in authorities) {
            return EventoAcesso.exigirCursoNoEscopo(cursoId, EventoAcesso.cursos(cursoEscopoPort, anfitriaoId))
        }
        if (cursoId == null) {
            throw DadoInvalidoException(EventoAcesso.MSG_CURSO_OBRIGATORIO)
        }
        val curso = cursoRepository.findById(cursoId)
            .orElseThrow { DadoInvalidoException("Curso do evento não foi encontrado.") }
        if (!curso.ativo) {
            throw DadoInvalidoException("O curso do público-alvo está inativo.")
        }
        return curso.id
    }
}
