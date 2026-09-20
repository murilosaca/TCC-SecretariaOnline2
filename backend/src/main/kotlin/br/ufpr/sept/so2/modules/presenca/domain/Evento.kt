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

    fun garantirConfirmacao(fase: FasePresenca, agora: OffsetDateTime, segredoValido: Boolean) {
        if (fase == FasePresenca.SAIDA && !attendanceMode.isDual()) {
            throw ConflitoEstadoException("Confirmação indisponível para este modo de presença.")
        }
        if (!janelaAtiva(fase, agora) || !pinCompativel(segredoValido)) {
            throw AcessoNegadoException(CONFIRMACAO_NEGADA)
        }
    }

    fun abrirJanela(fase: FasePresenca, novoHash: String?, agora: OffsetDateTime, minutos: Int) {
        garantirAbertura(fase, novoHash, minutos)
        pinHash = novoHash
        estado = EventoEstado.EM_ANDAMENTO
        if (fase == FasePresenca.SAIDA) {
            fecharJanelaSeAberta(FasePresenca.ENTRADA, agora)
        } else if (attendanceMode.isDual()) {
            fecharJanelaSeAberta(FasePresenca.SAIDA, agora)
        }
        aplicarJanela(fase, agora, minutos)
        updatedAt = agora
    }

    fun renovarSegredo(novoHash: String?, agora: OffsetDateTime) {
        if (estado == EventoEstado.CONCLUIDO) {
            throw ConflitoEstadoException("Evento já encerrado.")
        }
        if (!attendanceMode.isQr()) {
            throw ConflitoEstadoException("Renovação de QR indisponível para este modo de presença.")
        }
        if (faseDaJanelaAtiva(agora) == null) {
            throw ConflitoEstadoException("Renovação de QR exige janela ativa.")
        }
        if (novoHash.isNullOrBlank()) {
            throw DadoInvalidoException("Segredo da janela é obrigatório.")
        }
        pinHash = novoHash
        updatedAt = agora
    }

    fun encerrar(agora: OffsetDateTime) {
        if (estado == EventoEstado.CONCLUIDO) {
            throw ConflitoEstadoException("Evento já encerrado.")
        }
        estado = EventoEstado.CONCLUIDO
        fecharJanelaSeAberta(FasePresenca.ENTRADA, agora)
        fecharJanelaSeAberta(FasePresenca.SAIDA, agora)
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

    private fun garantirAbertura(fase: FasePresenca, novoHash: String?, minutos: Int) {
        if (estado == EventoEstado.CONCLUIDO) {
            throw ConflitoEstadoException("Evento já encerrado.")
        }
        if (fase == FasePresenca.SAIDA && !attendanceMode.isDual()) {
            throw ConflitoEstadoException("Abertura de janela indisponível para este modo de presença.")
        }
        if (fase == FasePresenca.SAIDA && janelaEntradaInicio == null) {
            throw ConflitoEstadoException("Abertura de janela de saída exige janela de entrada.")
        }
        if (novoHash.isNullOrBlank()) {
            throw DadoInvalidoException("Segredo da janela é obrigatório.")
        }
        if (minutos <= 0) {
            throw DadoInvalidoException("Duração da janela deve ser positiva.")
        }
    }

    private fun aplicarJanela(fase: FasePresenca, agora: OffsetDateTime, minutos: Int) {
        val fim = agora.plusMinutes(minutos.toLong())
        if (fase == FasePresenca.SAIDA) {
            janelaSaidaInicio = agora
            janelaSaidaFim = fim
        } else {
            janelaEntradaInicio = agora
            janelaEntradaFim = fim
        }
    }

    private fun fecharJanelaSeAberta(fase: FasePresenca, agora: OffsetDateTime) {
        val fechada = agora.minusSeconds(1)
        if (fase == FasePresenca.SAIDA) {
            if (janelaSaidaInicio == null) {
                return
            }
            if (janelaSaidaInicio!!.isAfter(fechada)) {
                janelaSaidaInicio = fechada
            }
            janelaSaidaFim = fechada
            return
        }
        if (janelaEntradaInicio == null) {
            return
        }
        if (janelaEntradaInicio!!.isAfter(fechada)) {
            janelaEntradaInicio = fechada
        }
        janelaEntradaFim = fechada
    }

    companion object {
        const val CONFIRMACAO_NEGADA = "Não foi possível confirmar a presença."
        const val JANELA_MINUTOS_PADRAO = 15
        const val QR_TTL_MINUTOS = 5L

        fun criar(
            id: UUID,
            idAnfitriao: UUID?,
            titulo: String,
            inicioEm: OffsetDateTime,
            fimEm: OffsetDateTime,
            cargaHoraria: Int,
            attendanceMode: AttendanceMode,
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
                attendanceMode,
                EventoEstado.AGENDADO,
                null,
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
