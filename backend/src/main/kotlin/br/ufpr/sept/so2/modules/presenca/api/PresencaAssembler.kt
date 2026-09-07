package br.ufpr.sept.so2.modules.presenca.api

import br.ufpr.sept.so2.modules.presenca.api.dto.EventoResponse
import br.ufpr.sept.so2.modules.presenca.api.dto.HostSessaoResponse
import br.ufpr.sept.so2.modules.presenca.api.dto.SessaoPresencaResponse
import br.ufpr.sept.so2.modules.presenca.application.AbrirJanelaEntradaUseCase.SessaoHost
import br.ufpr.sept.so2.modules.presenca.application.ObterSessaoPresencaUseCase.SessaoPresenca
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.modules.presenca.domain.SituacaoPresenca
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.UUID

@Component
class PresencaAssembler {

    fun fromEvento(
        evento: Evento,
        fases: List<FasePresenca>,
        authorities: List<String>,
        usuarioId: UUID,
        agora: OffsetDateTime,
    ): EventoResponse {
        val faseAtiva = evento.faseDaJanelaAtiva(agora)
        val links = linkedMapOf<String, String>()
        links["self"] = "/events/${evento.id}"
        if (AUTHORITY_VIEW in authorities) {
            links["sessao"] = "/events/${evento.id}/attendance/session"
        }
        if (podeConfirmarEntrada(evento, fases, authorities, agora)) {
            links["confirmar-entrada"] = "/events/${evento.id}/attendance/confirm"
        }
        acrescentarLinksHospedeiro(links, evento, authorities, usuarioId, agora)
        return EventoResponse(
            evento.id,
            evento.idAnfitriao,
            evento.titulo,
            evento.inicioEm,
            evento.fimEm,
            evento.cargaHoraria,
            evento.attendanceMode.name,
            evento.estado.name,
            situacao(evento, fases).name,
            faseAtiva != null,
            links,
        )
    }

    fun fromSessao(sessao: SessaoPresenca, authorities: List<String>, agora: OffsetDateTime): SessaoPresencaResponse {
        val evento = sessao.evento
        val fases = sessao.fasesConfirmadas
        val faseAtiva = evento.faseDaJanelaAtiva(agora)
        val links = linkedMapOf<String, String>()
        links["self"] = "/events/${evento.id}/attendance/session"
        links["evento"] = "/events/${evento.id}"
        if (podeConfirmarEntrada(evento, fases, authorities, agora)) {
            links["confirmar-entrada"] = "/events/${evento.id}/attendance/confirm"
        }
        if (podeConfirmarSaida(evento, fases, authorities, agora)) {
            links["confirmar-saida"] = "/events/${evento.id}/attendance/confirm"
        }
        return SessaoPresencaResponse(
            evento.id,
            evento.titulo,
            evento.attendanceMode.name,
            evento.estado.name,
            situacao(evento, fases).name,
            faseAtiva != null,
            if (faseAtiva == null) null else evento.fimJanela(faseAtiva),
            if (faseAtiva == null) null else faseAtiva.name,
            links,
        )
    }

    fun fromHost(sessao: SessaoHost, authorities: List<String>, usuarioId: UUID, agora: OffsetDateTime): HostSessaoResponse {
        val evento = sessao.evento
        val faseAtiva = evento.faseDaJanelaAtiva(agora)
        val links = linkedMapOf<String, String>()
        links["self"] = "/events/${evento.id}/attendance/host-session"
        links["evento"] = "/events/${evento.id}"
        acrescentarLinksHospedeiro(links, evento, authorities, usuarioId, agora)
        return HostSessaoResponse(
            evento.id,
            evento.titulo,
            evento.attendanceMode.name,
            evento.estado.name,
            faseAtiva != null,
            if (faseAtiva == null) null else evento.fimJanela(faseAtiva),
            sessao.pin,
            sessao.presentes,
            links,
        )
    }

    companion object {
        const val AUTHORITY_VIEW = "attendance.view_open"
        const val AUTHORITY_CHECK_IN = "attendance.check_in"
        const val AUTHORITY_MANAGE = "event.manage"
        const val AUTHORITY_HOST = "event.host"

        private fun acrescentarLinksHospedeiro(
            links: MutableMap<String, String>,
            evento: Evento,
            authorities: List<String>,
            usuarioId: UUID,
            agora: OffsetDateTime,
        ) {
            if (AUTHORITY_HOST !in authorities || !evento.eAnfitriao(usuarioId)) {
                return
            }
            links["host-session"] = "/events/${evento.id}/attendance/host-session"
            if (evento.attendanceMode.exercitadoNesteSprint() && evento.estado != EventoEstado.CONCLUIDO) {
                links["abrir-janela-entrada"] = "/events/${evento.id}/attendance/windows/entry"
            }
            if (evento.estado == EventoEstado.EM_ANDAMENTO) {
                links["encerrar-evento"] = "/events/${evento.id}/encerrar"
            }
        }

        private fun podeConfirmarEntrada(
            evento: Evento,
            fases: List<FasePresenca>,
            authorities: List<String>,
            agora: OffsetDateTime,
        ): Boolean = AUTHORITY_CHECK_IN in authorities &&
            evento.attendanceMode.exercitadoNesteSprint() &&
            evento.janelaAtiva(FasePresenca.ENTRADA, agora) &&
            FasePresenca.ENTRADA !in fases

        private fun podeConfirmarSaida(
            evento: Evento,
            fases: List<FasePresenca>,
            authorities: List<String>,
            agora: OffsetDateTime,
        ): Boolean = AUTHORITY_CHECK_IN in authorities &&
            evento.attendanceMode.isDual() &&
            evento.janelaAtiva(FasePresenca.SAIDA, agora) &&
            FasePresenca.ENTRADA in fases &&
            FasePresenca.SAIDA !in fases

        private fun situacao(evento: Evento, fases: List<FasePresenca>): SituacaoPresenca = when {
            FasePresenca.ENTRADA in fases && (!evento.attendanceMode.isDual() || FasePresenca.SAIDA in fases) ->
                SituacaoPresenca.COMPLETA
            FasePresenca.ENTRADA in fases -> SituacaoPresenca.PARCIAL
            else -> SituacaoPresenca.PENDENTE
        }
    }
}
