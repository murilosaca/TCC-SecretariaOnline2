package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence;

import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado;
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "evento")
public class EventoJpaEntity extends BaseEntity {

    @Column(name = "id_anfitriao")
    private UUID idAnfitriao;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(name = "inicio_em", nullable = false)
    private OffsetDateTime inicioEm;

    @Column(name = "fim_em", nullable = false)
    private OffsetDateTime fimEm;

    @Column(name = "carga_horaria", nullable = false)
    private int cargaHoraria;

    @Column(name = "attendance_mode", nullable = false, length = 20)
    private String attendanceMode;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "pin_hash", length = 255)
    private String pinHash;

    @Column(name = "janela_entrada_inicio")
    private OffsetDateTime janelaEntradaInicio;

    @Column(name = "janela_entrada_fim")
    private OffsetDateTime janelaEntradaFim;

    @Column(name = "janela_saida_inicio")
    private OffsetDateTime janelaSaidaInicio;

    @Column(name = "janela_saida_fim")
    private OffsetDateTime janelaSaidaFim;

    protected EventoJpaEntity() {
    }

    public static EventoJpaEntity fromDomain(Evento evento) {
        EventoJpaEntity entity = new EventoJpaEntity();
        entity.setId(evento.getId());
        entity.merge(evento);
        return entity;
    }

    public void merge(Evento evento) {
        this.idAnfitriao = evento.getIdAnfitriao();
        this.titulo = evento.getTitulo();
        this.inicioEm = evento.getInicioEm();
        this.fimEm = evento.getFimEm();
        this.cargaHoraria = evento.getCargaHoraria();
        this.attendanceMode = evento.getAttendanceMode().name();
        this.estado = evento.getEstado().name();
        this.pinHash = evento.getPinHash();
        this.janelaEntradaInicio = evento.getJanelaEntradaInicio();
        this.janelaEntradaFim = evento.getJanelaEntradaFim();
        this.janelaSaidaInicio = evento.getJanelaSaidaInicio();
        this.janelaSaidaFim = evento.getJanelaSaidaFim();
    }

    public String getTitulo() {
        return titulo;
    }

    public UUID getIdAnfitriao() {
        return idAnfitriao;
    }

    public Evento toDomain() {
        return new Evento(
                getId(),
                idAnfitriao,
                titulo,
                inicioEm,
                fimEm,
                cargaHoraria,
                AttendanceMode.from(attendanceMode),
                EventoEstado.from(estado),
                pinHash,
                janelaEntradaInicio,
                janelaEntradaFim,
                janelaSaidaInicio,
                janelaSaidaFim,
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
