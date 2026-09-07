package br.ufpr.sept.so2.modules.presenca.api;

import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal;
import br.ufpr.sept.so2.modules.presenca.api.dto.ConfirmarPresencaRequest;
import br.ufpr.sept.so2.modules.presenca.api.dto.CriarEventoRequest;
import br.ufpr.sept.so2.modules.presenca.api.dto.EventoResponse;
import br.ufpr.sept.so2.modules.presenca.api.dto.HostSessaoResponse;
import br.ufpr.sept.so2.modules.presenca.api.dto.SessaoPresencaResponse;
import br.ufpr.sept.so2.modules.presenca.application.AbrirJanelaEntradaUseCase;
import br.ufpr.sept.so2.modules.presenca.application.ConfirmarPresencaUseCase;
import br.ufpr.sept.so2.modules.presenca.application.CriarEventoUseCase;
import br.ufpr.sept.so2.modules.presenca.application.EncerrarEventoUseCase;
import br.ufpr.sept.so2.modules.presenca.application.ListarEventosDoAlunoUseCase;
import br.ufpr.sept.so2.modules.presenca.application.ListarEventosDoAnfitriaoUseCase;
import br.ufpr.sept.so2.modules.presenca.application.ObterEventoUseCase;
import br.ufpr.sept.so2.modules.presenca.application.ObterSessaoHostUseCase;
import br.ufpr.sept.so2.modules.presenca.application.ObterSessaoPresencaUseCase;
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca;
import br.ufpr.sept.so2.modules.presenca.domain.Presenca;
import br.ufpr.sept.so2.shared.api.PageResponse;
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException;
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@Tag(name = "Eventos e presença", description = "Proof of Stay v4.1 (RF-F1-009 / RF-F3-002)")
public class EventoController {

    private final ListarEventosDoAlunoUseCase listarEventosDoAlunoUseCase;
    private final ListarEventosDoAnfitriaoUseCase listarEventosDoAnfitriaoUseCase;
    private final CriarEventoUseCase criarEventoUseCase;
    private final ObterEventoUseCase obterEventoUseCase;
    private final ObterSessaoPresencaUseCase obterSessaoPresencaUseCase;
    private final ObterSessaoHostUseCase obterSessaoHostUseCase;
    private final AbrirJanelaEntradaUseCase abrirJanelaEntradaUseCase;
    private final EncerrarEventoUseCase encerrarEventoUseCase;
    private final ConfirmarPresencaUseCase confirmarPresencaUseCase;
    private final PresencaRepository presencaRepository;
    private final PresencaAssembler assembler;

    public EventoController(
            ListarEventosDoAlunoUseCase listarEventosDoAlunoUseCase,
            ListarEventosDoAnfitriaoUseCase listarEventosDoAnfitriaoUseCase,
            CriarEventoUseCase criarEventoUseCase,
            ObterEventoUseCase obterEventoUseCase,
            ObterSessaoPresencaUseCase obterSessaoPresencaUseCase,
            ObterSessaoHostUseCase obterSessaoHostUseCase,
            AbrirJanelaEntradaUseCase abrirJanelaEntradaUseCase,
            EncerrarEventoUseCase encerrarEventoUseCase,
            ConfirmarPresencaUseCase confirmarPresencaUseCase,
            PresencaRepository presencaRepository,
            PresencaAssembler assembler
    ) {
        this.listarEventosDoAlunoUseCase = listarEventosDoAlunoUseCase;
        this.listarEventosDoAnfitriaoUseCase = listarEventosDoAnfitriaoUseCase;
        this.criarEventoUseCase = criarEventoUseCase;
        this.obterEventoUseCase = obterEventoUseCase;
        this.obterSessaoPresencaUseCase = obterSessaoPresencaUseCase;
        this.obterSessaoHostUseCase = obterSessaoHostUseCase;
        this.abrirJanelaEntradaUseCase = abrirJanelaEntradaUseCase;
        this.encerrarEventoUseCase = encerrarEventoUseCase;
        this.confirmarPresencaUseCase = confirmarPresencaUseCase;
        this.presencaRepository = presencaRepository;
        this.assembler = assembler;
    }

    @GetMapping(params = "mine=true")
    @PreAuthorize("hasAuthority('event.manage')")
    @Operation(summary = "Listar eventos do anfitrião")
    public PageResponse<EventoResponse> listarMeus(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        IamPrincipal principal = principal(authentication);
        OffsetDateTime agora = OffsetDateTime.now();
        return PageResponse.ofWithLinks(
                listarEventosDoAnfitriaoUseCase.execute(principal.userId(), pageable),
                evento -> assembler.fromEvento(evento, List.of(), principal.authorities(), principal.userId(), agora),
                Map.of("novoEvento", "/events")
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('attendance.view_open')")
    @Operation(summary = "Listar eventos disponíveis para o aluno")
    public PageResponse<EventoResponse> listar(
            @RequestParam(defaultValue = "me") String audience,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        IamPrincipal principal = principal(authentication);
        OffsetDateTime agora = OffsetDateTime.now();
        return PageResponse.ofWithLinks(
                listarEventosDoAlunoUseCase.execute(audience, pageable),
                evento -> assembler.fromEvento(
                        evento,
                        fases(evento.getId(), principal.userId()),
                        principal.authorities(),
                        principal.userId(),
                        agora
                )
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('event.manage')")
    @Operation(summary = "Criar evento SECRET_SINGLE")
    public EventoResponse criar(@Valid @RequestBody CriarEventoRequest request, Authentication authentication) {
        IamPrincipal principal = principal(authentication);
        Evento criado = criarEventoUseCase.execute(
                principal.userId(),
                request.titulo(),
                request.inicioEm(),
                request.fimEm(),
                request.cargaHoraria()
        );
        return assembler.fromEvento(criado, List.of(), principal.authorities(), principal.userId(), OffsetDateTime.now());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('attendance.view_open','event.manage')")
    @Operation(summary = "Detalhe do evento")
    public EventoResponse buscar(@PathVariable UUID id, Authentication authentication) {
        IamPrincipal principal = principal(authentication);
        Evento evento = obterEventoUseCase.execute(id);
        garantirLeitura(evento, principal);
        return assembler.fromEvento(
                evento,
                fases(id, principal.userId()),
                principal.authorities(),
                principal.userId(),
                OffsetDateTime.now()
        );
    }

    @GetMapping("/{id}/attendance/session")
    @PreAuthorize("hasAuthority('attendance.view_open')")
    @Operation(summary = "Sessão de presença do aluno")
    public SessaoPresencaResponse sessao(@PathVariable UUID id, Authentication authentication) {
        IamPrincipal principal = principal(authentication);
        return assembler.fromSessao(
                obterSessaoPresencaUseCase.execute(id, principal.userId()),
                principal.authorities(),
                OffsetDateTime.now()
        );
    }

    @GetMapping("/{id}/attendance/host-session")
    @PreAuthorize("hasAuthority('event.host')")
    @Operation(summary = "Sessão de operação do anfitrião (PIN em claro)")
    public HostSessaoResponse sessaoHost(@PathVariable UUID id, Authentication authentication) {
        IamPrincipal principal = principal(authentication);
        return assembler.fromHost(
                obterSessaoHostUseCase.execute(id, principal.userId()),
                principal.authorities(),
                principal.userId(),
                OffsetDateTime.now()
        );
    }

    @PostMapping("/{id}/attendance/windows/entry")
    @PreAuthorize("hasAuthority('event.host')")
    @Operation(summary = "Abrir/renovar janela de entrada SECRET_SINGLE")
    public HostSessaoResponse abrirJanelaEntrada(@PathVariable UUID id, Authentication authentication) {
        IamPrincipal principal = principal(authentication);
        return assembler.fromHost(
                abrirJanelaEntradaUseCase.execute(id, principal.userId()),
                principal.authorities(),
                principal.userId(),
                OffsetDateTime.now()
        );
    }

    @PostMapping("/{id}/attendance/windows/exit")
    @PreAuthorize("hasAuthority('event.host')")
    @Operation(summary = "Saída não exercitada neste sprint")
    public HostSessaoResponse abrirJanelaSaida(@PathVariable UUID id) {
        throw new ConflitoEstadoException("Abertura de janela indisponível para este modo de presença.");
    }

    @PostMapping("/{id}/encerrar")
    @PreAuthorize("hasAuthority('event.host')")
    @Operation(summary = "Encerrar evento")
    public HostSessaoResponse encerrar(@PathVariable UUID id, Authentication authentication) {
        IamPrincipal principal = principal(authentication);
        return assembler.fromHost(
                encerrarEventoUseCase.execute(id, principal.userId()),
                principal.authorities(),
                principal.userId(),
                OffsetDateTime.now()
        );
    }

    @PostMapping("/{id}/attendance/confirm")
    @PreAuthorize("hasAuthority('attendance.check_in')")
    @Operation(summary = "Confirmar presença (SECRET_SINGLE neste sprint)")
    public SessaoPresencaResponse confirmar(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmarPresencaRequest request,
            Authentication authentication
    ) {
        IamPrincipal principal = principal(authentication);
        return assembler.fromSessao(
                confirmarPresencaUseCase.execute(
                        id,
                        principal.userId(),
                        request.pin(),
                        request.deviceUuid(),
                        request.fase()
                ),
                principal.authorities(),
                OffsetDateTime.now()
        );
    }

    private void garantirLeitura(Evento evento, IamPrincipal principal) {
        if (principal.authorities().contains(PresencaAssembler.AUTHORITY_VIEW)) {
            return;
        }
        if (principal.authorities().contains(PresencaAssembler.AUTHORITY_MANAGE) && evento.eAnfitriao(principal.userId())) {
            return;
        }
        throw new AcessoNegadoException("Você não pode acessar este evento.");
    }

    private List<FasePresenca> fases(UUID eventoId, UUID usuarioId) {
        return presencaRepository.findByEventoAndUsuario(eventoId, usuarioId).stream()
                .map(Presenca::getFase)
                .toList();
    }

    private static IamPrincipal principal(Authentication authentication) {
        return (IamPrincipal) authentication.getPrincipal();
    }
}
