package br.ufpr.sept.so2.modules.presenca.application

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
) {
    @Transactional
    fun execute(
        anfitriaoId: UUID,
        titulo: String,
        inicioEm: OffsetDateTime,
        fimEm: OffsetDateTime,
        cargaHoraria: Int,
        attendanceModeRaw: String?,
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
        ),
    )
}
