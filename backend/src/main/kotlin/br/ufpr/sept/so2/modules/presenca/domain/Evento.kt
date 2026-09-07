package br.ufpr.sept.so2.modules.presenca.domain

import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.time.OffsetDateTime
import java.util.UUID

class Evento(
    val id: UUID,
    var idAnfitriao: UUID?,
    var titulo: String,
    var inicioEm: OffsetDateTime,
    var fimEm: OffsetDateTime,
    var cargaHoraria: Int,
    var attendanceMode: AttendanceMode,
    var estado: EventoEstado,
    var pinHash: String?,
    var janelaEntradaInicio: OffsetDateTime?,
    var janelaEntradaFim: OffsetDateTime?,
    var janelaSaidaInicio: OffsetDateTime?,
    var janelaSaidaFim: OffsetDateTime?,
    val createdAt: OffsetDateTime,
    var updatedAt: OffsetDateTime,
) {
    init {
        validarIntervalo(inicioEm, fimEm, cargaHoraria, titulo)
    }

    fun eAnfitriao(usuarioId: UUID?): Boolean = idAnfitriao != null && idAnfitriao == usuarioId

    fun garantirHospedeiro(usuarioId: UUID?) {
        if (!eAnfitriao(usuarioId)) {
            throw AcessoNegadoException("Somente o anfitrião pode operar este evento.")
        }
    }

    fun janelaAtiva(fase: FasePresenca, agora: OffsetDateTime?): Boolean {
        val inicio = inicioJanela(fase)
        val fim = fimJanela(fase)
        if (inicio == null || fim == null || agora == null) {
            return false
        }
        return !agora.isBefore(inicio) && !agora.isAfter(fim)
    }

    fun fimJanela(fase: FasePresenca): OffsetDateTime? =
        when (fase) {
            FasePresenca.SAIDA -> janelaSaidaFim
            FasePresenca.ENTRADA -> janelaEntradaFim
        }

    fun inicioJanela(fase: FasePresenca): OffsetDateTime? =
        when (fase) {
            FasePresenca.SAIDA -> janelaSaidaInicio
            FasePresenca.ENTRADA -> janelaEntradaInicio
        }

    fun pinCompativel(matches: Boolean): Boolean = !pinHash.isNullOrBlank() && matches

    fun garantirConfirmacao(
        modoInformado: AttendanceMode,
        fase: FasePresenca,
        agora: OffsetDateTime,
        pinValido: Boolean,
    ) {
        if (attendanceMode != AttendanceMode.SECRET_SINGLE || modoInformado != AttendanceMode.SECRET_SINGLE) {
            throw ConflitoEstadoException("Confirmação indisponível para este modo de presença.")
        }
        if (fase != FasePresenca.ENTRADA) {
            throw ConflitoEstadoException("Confirmação indisponível para este modo de presença.")
        }
        if (!janelaAtiva(fase, agora) || !pinCompativel(pinValido)) {
            throw AcessoNegadoException(CONFIRMACAO_NEGADA)
        }
    }

    fun abrirJanelaEntrada(novoPinHash: String?, agora: OffsetDateTime, minutos: Int) {
        if (estado == EventoEstado.CONCLUIDO) {
            throw ConflitoEstadoException("Evento já encerrado.")
        }
        if (attendanceMode != AttendanceMode.SECRET_SINGLE) {
            throw ConflitoEstadoException("Abertura de janela indisponível para este modo de presença.")
        }
        if (novoPinHash.isNullOrBlank()) {
            throw DadoInvalidoException("PIN do evento é obrigatório.")
        }
        if (minutos <= 0) {
            throw DadoInvalidoException("Duração da janela deve ser positiva.")
        }
        pinHash = novoPinHash
        estado = EventoEstado.EM_ANDAMENTO
        janelaEntradaInicio = agora
        janelaEntradaFim = agora.plusMinutes(minutos.toLong())
        updatedAt = agora
    }

    fun encerrar(agora: OffsetDateTime) {
        if (estado == EventoEstado.CONCLUIDO) {
            throw ConflitoEstadoException("Evento já encerrado.")
        }
        estado = EventoEstado.CONCLUIDO
        val fechada = agora.minusSeconds(1)
        if (janelaEntradaInicio == null || janelaEntradaInicio!!.isAfter(fechada)) {
            janelaEntradaInicio = fechada
        }
        janelaEntradaFim = fechada
        if (janelaSaidaInicio != null) {
            janelaSaidaFim = fechada
        }
        updatedAt = agora
    }

    fun faseDaJanelaAtiva(agora: OffsetDateTime): FasePresenca? {
        if (janelaAtiva(FasePresenca.ENTRADA, agora)) {
            return FasePresenca.ENTRADA
        }
        if (attendanceMode.isDual() && janelaAtiva(FasePresenca.SAIDA, agora)) {
            return FasePresenca.SAIDA
        }
        return null
    }

    fun renovarJanelaDev(pinHash: String, idAnfitriao: UUID?, agora: OffsetDateTime) {
        this.pinHash = pinHash
        this.idAnfitriao = idAnfitriao
        estado = EventoEstado.EM_ANDAMENTO
        inicioEm = agora.minusMinutes(5)
        fimEm = agora.plusHours(2)
        janelaEntradaInicio = agora
        janelaEntradaFim = agora.plusMinutes(30)
        updatedAt = agora
    }

    companion object {
        const val CONFIRMACAO_NEGADA = "Não foi possível confirmar a presença."
        const val JANELA_ENTRADA_MINUTOS_PADRAO = 15

        fun criar(
            id: UUID,
            idAnfitriao: UUID?,
            titulo: String,
            inicioEm: OffsetDateTime,
            fimEm: OffsetDateTime,
            cargaHoraria: Int,
            pinHash: String,
            agora: OffsetDateTime,
        ): Evento {
            if (idAnfitriao == null) {
                throw DadoInvalidoException("Anfitrião do evento é obrigatório.")
            }
            return Evento(
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
                agora,
            )
        }

        fun seedDev(id: UUID, idAnfitriao: UUID?, titulo: String, pinHash: String, agora: OffsetDateTime): Evento =
            Evento(
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
                agora,
            )

        private fun validarIntervalo(
            inicioEm: OffsetDateTime?,
            fimEm: OffsetDateTime?,
            cargaHoraria: Int,
            titulo: String?,
        ) {
            if (titulo.isNullOrBlank()) {
                throw DadoInvalidoException("Título do evento é obrigatório.")
            }
            if (inicioEm == null || fimEm == null || !fimEm.isAfter(inicioEm)) {
                throw DadoInvalidoException("O fim do evento deve ser posterior ao início.")
            }
            if (cargaHoraria <= 0) {
                throw DadoInvalidoException("Carga horária deve ser positiva.")
            }
        }
    }
}
