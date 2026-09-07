package br.ufpr.sept.so2.modules.presenca.domain;

import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException;
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Evento {

    public static final String CONFIRMACAO_NEGADA = "Não foi possível confirmar a presença.";
    public static final int JANELA_ENTRADA_MINUTOS_PADRAO = 15;

    private final UUID id;
    private UUID idAnfitriao;
    private String titulo;
    private OffsetDateTime inicioEm;
    private OffsetDateTime fimEm;
    private int cargaHoraria;
    private AttendanceMode attendanceMode;
    private EventoEstado estado;
    private String pinHash;
    private OffsetDateTime janelaEntradaInicio;
    private OffsetDateTime janelaEntradaFim;
    private OffsetDateTime janelaSaidaInicio;
    private OffsetDateTime janelaSaidaFim;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Evento(
            UUID id,
            UUID idAnfitriao,
            String titulo,
            OffsetDateTime inicioEm,
            OffsetDateTime fimEm,
            int cargaHoraria,
            AttendanceMode attendanceMode,
            EventoEstado estado,
            String pinHash,
            OffsetDateTime janelaEntradaInicio,
            OffsetDateTime janelaEntradaFim,
            OffsetDateTime janelaSaidaInicio,
            OffsetDateTime janelaSaidaFim,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        validarIntervalo(inicioEm, fimEm, cargaHoraria, titulo);
        this.id = id;
        this.idAnfitriao = idAnfitriao;
        this.titulo = titulo;
        this.inicioEm = inicioEm;
        this.fimEm = fimEm;
        this.cargaHoraria = cargaHoraria;
        this.attendanceMode = attendanceMode;
        this.estado = estado;
        this.pinHash = pinHash;
        this.janelaEntradaInicio = janelaEntradaInicio;
        this.janelaEntradaFim = janelaEntradaFim;
        this.janelaSaidaInicio = janelaSaidaInicio;
        this.janelaSaidaFim = janelaSaidaFim;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Evento criar(
            UUID id,
            UUID idAnfitriao,
            String titulo,
            OffsetDateTime inicioEm,
            OffsetDateTime fimEm,
            int cargaHoraria,
            String pinHash,
            OffsetDateTime agora
    ) {
        if (idAnfitriao == null) {
            throw new DadoInvalidoException("Anfitrião do evento é obrigatório.");
        }
        return new Evento(
                id,
                idAnfitriao,
                titulo,
                inicioEm,
                fimEm,
                cargaHoraria,
                AttendanceMode.SECRET_SINGLE,
                EventoEstado.AGENDADO,
                pinHash,
                null,
                null,
                null,
                null,
                agora,
                agora
        );
    }

    public static Evento seedDev(UUID id, UUID idAnfitriao, String titulo, String pinHash, OffsetDateTime agora) {
        return new Evento(
                id,
                idAnfitriao,
                titulo,
                agora.minusMinutes(5),
                agora.plusHours(2),
                4,
                AttendanceMode.SECRET_SINGLE,
                EventoEstado.EM_ANDAMENTO,
                pinHash,
                agora,
                agora.plusMinutes(30),
                null,
                null,
                agora,
                agora
        );
    }

    public void renovarJanelaDev(String pinHash, UUID idAnfitriao, OffsetDateTime agora) {
        this.pinHash = pinHash;
        this.idAnfitriao = idAnfitriao;
        this.estado = EventoEstado.EM_ANDAMENTO;
        this.inicioEm = agora.minusMinutes(5);
        this.fimEm = agora.plusHours(2);
        this.janelaEntradaInicio = agora;
        this.janelaEntradaFim = agora.plusMinutes(30);
        this.updatedAt = agora;
    }

    public boolean eAnfitriao(UUID usuarioId) {
        return idAnfitriao != null && idAnfitriao.equals(usuarioId);
    }

    public void garantirHospedeiro(UUID usuarioId) {
        if (!eAnfitriao(usuarioId)) {
            throw new AcessoNegadoException("Somente o anfitrião pode operar este evento.");
        }
    }

    public boolean janelaAtiva(FasePresenca fase, OffsetDateTime agora) {
        OffsetDateTime inicio = inicioJanela(fase);
        OffsetDateTime fim = fimJanela(fase);
        if (inicio == null || fim == null || agora == null) {
            return false;
        }
        return !agora.isBefore(inicio) && !agora.isAfter(fim);
    }

    public OffsetDateTime fimJanela(FasePresenca fase) {
        return fase == FasePresenca.SAIDA ? janelaSaidaFim : janelaEntradaFim;
    }

    public OffsetDateTime inicioJanela(FasePresenca fase) {
        return fase == FasePresenca.SAIDA ? janelaSaidaInicio : janelaEntradaInicio;
    }

    public boolean pinCompativel(boolean matches) {
        return pinHash != null && !pinHash.isBlank() && matches;
    }

    public void garantirConfirmacao(
            AttendanceMode modoInformado,
            FasePresenca fase,
            OffsetDateTime agora,
            boolean pinValido
    ) {
        if (attendanceMode != AttendanceMode.SECRET_SINGLE || modoInformado != AttendanceMode.SECRET_SINGLE) {
            throw new ConflitoEstadoException("Confirmação indisponível para este modo de presença.");
        }
        if (fase != FasePresenca.ENTRADA) {
            throw new ConflitoEstadoException("Confirmação indisponível para este modo de presença.");
        }
        if (!janelaAtiva(fase, agora) || !pinCompativel(pinValido)) {
            throw new AcessoNegadoException(CONFIRMACAO_NEGADA);
        }
    }

    public void abrirJanelaEntrada(String novoPinHash, OffsetDateTime agora, int minutos) {
        if (estado == EventoEstado.CONCLUIDO) {
            throw new ConflitoEstadoException("Evento já encerrado.");
        }
        if (attendanceMode != AttendanceMode.SECRET_SINGLE) {
            throw new ConflitoEstadoException("Abertura de janela indisponível para este modo de presença.");
        }
        if (novoPinHash == null || novoPinHash.isBlank()) {
            throw new DadoInvalidoException("PIN do evento é obrigatório.");
        }
        if (minutos <= 0) {
            throw new DadoInvalidoException("Duração da janela deve ser positiva.");
        }
        this.pinHash = novoPinHash;
        this.estado = EventoEstado.EM_ANDAMENTO;
        this.janelaEntradaInicio = agora;
        this.janelaEntradaFim = agora.plusMinutes(minutos);
        this.updatedAt = agora;
    }

    public void encerrar(OffsetDateTime agora) {
        if (estado == EventoEstado.CONCLUIDO) {
            throw new ConflitoEstadoException("Evento já encerrado.");
        }
        this.estado = EventoEstado.CONCLUIDO;
        OffsetDateTime fechada = agora.minusSeconds(1);
        if (janelaEntradaInicio == null || janelaEntradaInicio.isAfter(fechada)) {
            this.janelaEntradaInicio = fechada;
        }
        this.janelaEntradaFim = fechada;
        if (janelaSaidaInicio != null) {
            this.janelaSaidaFim = fechada;
        }
        this.updatedAt = agora;
    }

    public FasePresenca faseDaJanelaAtiva(OffsetDateTime agora) {
        if (janelaAtiva(FasePresenca.ENTRADA, agora)) {
            return FasePresenca.ENTRADA;
        }
        if (attendanceMode.isDual() && janelaAtiva(FasePresenca.SAIDA, agora)) {
            return FasePresenca.SAIDA;
        }
        return null;
    }

    private static void validarIntervalo(
            OffsetDateTime inicioEm,
            OffsetDateTime fimEm,
            int cargaHoraria,
            String titulo
    ) {
        if (titulo == null || titulo.isBlank()) {
            throw new DadoInvalidoException("Título do evento é obrigatório.");
        }
        if (inicioEm == null || fimEm == null || !fimEm.isAfter(inicioEm)) {
            throw new DadoInvalidoException("O fim do evento deve ser posterior ao início.");
        }
        if (cargaHoraria <= 0) {
            throw new DadoInvalidoException("Carga horária deve ser positiva.");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getIdAnfitriao() {
        return idAnfitriao;
    }

    public String getTitulo() {
        return titulo;
    }

    public OffsetDateTime getInicioEm() {
        return inicioEm;
    }

    public OffsetDateTime getFimEm() {
        return fimEm;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public AttendanceMode getAttendanceMode() {
        return attendanceMode;
    }

    public EventoEstado getEstado() {
        return estado;
    }

    public String getPinHash() {
        return pinHash;
    }

    public OffsetDateTime getJanelaEntradaInicio() {
        return janelaEntradaInicio;
    }

    public OffsetDateTime getJanelaEntradaFim() {
        return janelaEntradaFim;
    }

    public OffsetDateTime getJanelaSaidaInicio() {
        return janelaSaidaInicio;
    }

    public OffsetDateTime getJanelaSaidaFim() {
        return janelaSaidaFim;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
