package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence

import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "evento")
class EventoJpaEntity : BaseEntity() {

    @Column(name = "id_anfitriao")
    var idAnfitriao: UUID? = null

    @Column(nullable = false, length = 200)
    var titulo: String? = null

    @Column(name = "inicio_em", nullable = false)
    var inicioEm: OffsetDateTime? = null

    @Column(name = "fim_em", nullable = false)
    var fimEm: OffsetDateTime? = null

    @Column(name = "carga_horaria", nullable = false)
    var cargaHoraria: Int = 0

    @Column(name = "attendance_mode", nullable = false, length = 20)
    var attendanceMode: String? = null

    @Column(nullable = false, length = 20)
    var estado: String? = null

    @Column(name = "pin_hash", length = 255)
    var pinHash: String? = null

    @Column(name = "janela_entrada_inicio")
    var janelaEntradaInicio: OffsetDateTime? = null

    @Column(name = "janela_entrada_fim")
    var janelaEntradaFim: OffsetDateTime? = null

    @Column(name = "janela_saida_inicio")
    var janelaSaidaInicio: OffsetDateTime? = null

    @Column(name = "janela_saida_fim")
    var janelaSaidaFim: OffsetDateTime? = null

    fun merge(evento: Evento) {
        idAnfitriao = evento.idAnfitriao
        titulo = evento.titulo
        inicioEm = evento.inicioEm
        fimEm = evento.fimEm
        cargaHoraria = evento.cargaHoraria
        attendanceMode = evento.attendanceMode.name
        estado = evento.estado.name
        pinHash = evento.pinHash
        janelaEntradaInicio = evento.janelaEntradaInicio
        janelaEntradaFim = evento.janelaEntradaFim
        janelaSaidaInicio = evento.janelaSaidaInicio
        janelaSaidaFim = evento.janelaSaidaFim
    }

    fun toDomain(): Evento = Evento(
        id!!,
        idAnfitriao,
        titulo!!,
        inicioEm!!,
        fimEm!!,
        cargaHoraria,
        AttendanceMode.from(attendanceMode),
        EventoEstado.from(estado),
        pinHash,
        janelaEntradaInicio,
        janelaEntradaFim,
        janelaSaidaInicio,
        janelaSaidaFim,
        createdAt!!,
        updatedAt!!,
    )

    companion object {
        fun fromDomain(evento: Evento): EventoJpaEntity {
            val entity = EventoJpaEntity()
            entity.id = evento.id
            entity.merge(evento)
            return entity
        }
    }
}
