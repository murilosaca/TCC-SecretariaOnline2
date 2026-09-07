package br.ufpr.sept.so2.modules.presenca.api;

import br.ufpr.sept.so2.modules.presenca.api.dto.EventoResponse;
import br.ufpr.sept.so2.modules.presenca.api.dto.HostSessaoResponse;
import br.ufpr.sept.so2.modules.presenca.api.dto.SessaoPresencaResponse;
import br.ufpr.sept.so2.modules.presenca.application.AbrirJanelaEntradaUseCase.SessaoHost;
import br.ufpr.sept.so2.modules.presenca.application.ObterSessaoPresencaUseCase.SessaoPresenca;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado;
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca;
import br.ufpr.sept.so2.modules.presenca.domain.SituacaoPresenca;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PresencaAssembler {

    public static final String AUTHORITY_VIEW = "attendance.view_open";
    public static final String AUTHORITY_CHECK_IN = "attendance.check_in";
    public static final String AUTHORITY_MANAGE = "event.manage";
    public static final String AUTHORITY_HOST = "event.host";

    public EventoResponse fromEvento(
            Evento evento,
            List<FasePresenca> fases,
            List<String> authorities,
            UUID usuarioId,
            OffsetDateTime agora
    ) {
        FasePresenca faseAtiva = evento.faseDaJanelaAtiva(agora);
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/events/" + evento.getId());
        if (authorities.contains(AUTHORITY_VIEW)) {
            links.put("sessao", "/events/" + evento.getId() + "/attendance/session");
        }
        if (podeConfirmarEntrada(evento, fases, authorities, agora)) {
            links.put("confirmar-entrada", "/events/" + evento.getId() + "/attendance/confirm");
        }
        acrescentarLinksHospedeiro(links, evento, authorities, usuarioId, agora);
        return new EventoResponse(
                evento.getId(),
                evento.getIdAnfitriao(),
                evento.getTitulo(),
                evento.getInicioEm(),
                evento.getFimEm(),
                evento.getCargaHoraria(),
                evento.getAttendanceMode().name(),
                evento.getEstado().name(),
                situacao(evento, fases).name(),
                faseAtiva != null,
                links
        );
    }

    public SessaoPresencaResponse fromSessao(SessaoPresenca sessao, List<String> authorities, OffsetDateTime agora) {
        Evento evento = sessao.evento();
        List<FasePresenca> fases = sessao.fasesConfirmadas();
        FasePresenca faseAtiva = evento.faseDaJanelaAtiva(agora);
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/events/" + evento.getId() + "/attendance/session");
        links.put("evento", "/events/" + evento.getId());
        if (podeConfirmarEntrada(evento, fases, authorities, agora)) {
            links.put("confirmar-entrada", "/events/" + evento.getId() + "/attendance/confirm");
        }
        if (podeConfirmarSaida(evento, fases, authorities, agora)) {
            links.put("confirmar-saida", "/events/" + evento.getId() + "/attendance/confirm");
        }
        return new SessaoPresencaResponse(
                evento.getId(),
                evento.getTitulo(),
                evento.getAttendanceMode().name(),
                evento.getEstado().name(),
                situacao(evento, fases).name(),
                faseAtiva != null,
                faseAtiva == null ? null : evento.fimJanela(faseAtiva),
                faseAtiva == null ? null : faseAtiva.name(),
                links
        );
    }

    public HostSessaoResponse fromHost(SessaoHost sessao, List<String> authorities, UUID usuarioId, OffsetDateTime agora) {
        Evento evento = sessao.evento();
        FasePresenca faseAtiva = evento.faseDaJanelaAtiva(agora);
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/events/" + evento.getId() + "/attendance/host-session");
        links.put("evento", "/events/" + evento.getId());
        acrescentarLinksHospedeiro(links, evento, authorities, usuarioId, agora);
        return new HostSessaoResponse(
                evento.getId(),
                evento.getTitulo(),
                evento.getAttendanceMode().name(),
                evento.getEstado().name(),
                faseAtiva != null,
                faseAtiva == null ? null : evento.fimJanela(faseAtiva),
                sessao.pin(),
                sessao.presentes(),
                links
        );
    }

    private static void acrescentarLinksHospedeiro(
            Map<String, String> links,
            Evento evento,
            List<String> authorities,
            UUID usuarioId,
            OffsetDateTime agora
    ) {
        if (!authorities.contains(AUTHORITY_HOST) || !evento.eAnfitriao(usuarioId)) {
            return;
        }
        links.put("host-session", "/events/" + evento.getId() + "/attendance/host-session");
        if (evento.getAttendanceMode().exercitadoNesteSprint() && evento.getEstado() != EventoEstado.CONCLUIDO) {
            links.put("abrir-janela-entrada", "/events/" + evento.getId() + "/attendance/windows/entry");
        }
        if (evento.getEstado() == EventoEstado.EM_ANDAMENTO) {
            links.put("encerrar-evento", "/events/" + evento.getId() + "/encerrar");
        }
    }

    private static boolean podeConfirmarEntrada(
            Evento evento,
            List<FasePresenca> fases,
            List<String> authorities,
            OffsetDateTime agora
    ) {
        return authorities.contains(AUTHORITY_CHECK_IN)
                && evento.getAttendanceMode().exercitadoNesteSprint()
                && evento.janelaAtiva(FasePresenca.ENTRADA, agora)
                && !fases.contains(FasePresenca.ENTRADA);
    }

    private static boolean podeConfirmarSaida(
            Evento evento,
            List<FasePresenca> fases,
            List<String> authorities,
            OffsetDateTime agora
    ) {
        return authorities.contains(AUTHORITY_CHECK_IN)
                && evento.getAttendanceMode().isDual()
                && evento.janelaAtiva(FasePresenca.SAIDA, agora)
                && fases.contains(FasePresenca.ENTRADA)
                && !fases.contains(FasePresenca.SAIDA);
    }

    private static SituacaoPresenca situacao(Evento evento, List<FasePresenca> fases) {
        if (fases.contains(FasePresenca.ENTRADA) && (!evento.getAttendanceMode().isDual() || fases.contains(FasePresenca.SAIDA))) {
            return SituacaoPresenca.COMPLETA;
        }
        if (fases.contains(FasePresenca.ENTRADA)) {
            return SituacaoPresenca.PARCIAL;
        }
        return SituacaoPresenca.PENDENTE;
    }
}
