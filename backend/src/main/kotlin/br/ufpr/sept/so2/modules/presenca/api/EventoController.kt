package br.ufpr.sept.so2.modules.presenca.api

import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.modules.presenca.api.dto.ConfirmarPresencaRequest
import br.ufpr.sept.so2.modules.presenca.api.dto.CriarEventoRequest
import br.ufpr.sept.so2.modules.presenca.api.dto.EventoResponse
import br.ufpr.sept.so2.modules.presenca.api.dto.HostSessaoResponse
import br.ufpr.sept.so2.modules.presenca.api.dto.SessaoPresencaResponse
import br.ufpr.sept.so2.modules.presenca.application.AbrirJanelaEntradaUseCase
import br.ufpr.sept.so2.modules.presenca.application.ConfirmarPresencaUseCase
import br.ufpr.sept.so2.modules.presenca.application.CriarEventoUseCase
import br.ufpr.sept.so2.modules.presenca.application.EncerrarEventoUseCase
import br.ufpr.sept.so2.modules.presenca.application.ListarEventosDoAlunoUseCase
import br.ufpr.sept.so2.modules.presenca.application.ListarEventosDoAnfitriaoUseCase
import br.ufpr.sept.so2.modules.presenca.application.ObterEventoUseCase
import br.ufpr.sept.so2.modules.presenca.application.ObterSessaoHostUseCase
import br.ufpr.sept.so2.modules.presenca.application.ObterSessaoPresencaUseCase
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.shared.api.PageResponse
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/events")
@Tag(name = "Eventos e presença", description = "Proof of Stay v4.1 (RF-F1-009 / RF-F3-002)")
class EventoController(
    private val listarEventosDoAlunoUseCase: ListarEventosDoAlunoUseCase,
    private val listarEventosDoAnfitriaoUseCase: ListarEventosDoAnfitriaoUseCase,
    private val criarEventoUseCase: CriarEventoUseCase,
    private val obterEventoUseCase: ObterEventoUseCase,
    private val obterSessaoPresencaUseCase: ObterSessaoPresencaUseCase,
    private val obterSessaoHostUseCase: ObterSessaoHostUseCase,
    private val abrirJanelaEntradaUseCase: AbrirJanelaEntradaUseCase,
    private val encerrarEventoUseCase: EncerrarEventoUseCase,
    private val confirmarPresencaUseCase: ConfirmarPresencaUseCase,
    private val presencaRepository: PresencaRepository,
    private val assembler: PresencaAssembler,
) {

    @GetMapping(params = ["mine=true"])
    @PreAuthorize("hasAuthority('event.manage')")
    @Operation(summary = "Listar eventos do anfitrião")
    fun listarMeus(
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<EventoResponse> {
        val principal = principal(authentication)
        val agora = OffsetDateTime.now()
        return PageResponse.ofWithLinks(
            listarEventosDoAnfitriaoUseCase.execute(principal.userId, pageable),
            Function { evento ->
                assembler.fromEvento(evento, emptyList(), principal.authorities, principal.userId, agora)
            },
            mapOf("novoEvento" to "/events"),
        )
    }

    @GetMapping
    @PreAuthorize("hasAuthority('attendance.view_open')")
    @Operation(summary = "Listar eventos disponíveis para o aluno")
    fun listar(
        @RequestParam(defaultValue = "me") audience: String,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<EventoResponse> {
        val principal = principal(authentication)
        val agora = OffsetDateTime.now()
        return PageResponse.ofWithLinks(
            listarEventosDoAlunoUseCase.execute(audience, pageable),
            Function { evento ->
                assembler.fromEvento(
                    evento,
                    fases(evento.id, principal.userId),
                    principal.authorities,
                    principal.userId,
                    agora,
                )
            },
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('event.manage')")
    @Operation(summary = "Criar evento SECRET_SINGLE")
    fun criar(@Valid @RequestBody request: CriarEventoRequest, authentication: Authentication): EventoResponse {
        val principal = principal(authentication)
        val criado = criarEventoUseCase.execute(
            principal.userId,
            request.titulo,
            request.inicioEm,
            request.fimEm,
            request.cargaHoraria,
        )
        return assembler.fromEvento(criado, emptyList(), principal.authorities, principal.userId, OffsetDateTime.now())
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('attendance.view_open','event.manage')")
    @Operation(summary = "Detalhe do evento")
    fun buscar(@PathVariable id: UUID, authentication: Authentication): EventoResponse {
        val principal = principal(authentication)
        val evento = obterEventoUseCase.execute(id)
        garantirLeitura(evento, principal)
        return assembler.fromEvento(
            evento,
            fases(id, principal.userId),
            principal.authorities,
            principal.userId,
            OffsetDateTime.now(),
        )
    }

    @GetMapping("/{id}/attendance/session")
    @PreAuthorize("hasAuthority('attendance.view_open')")
    @Operation(summary = "Sessão de presença do aluno")
    fun sessao(@PathVariable id: UUID, authentication: Authentication): SessaoPresencaResponse {
        val principal = principal(authentication)
        return assembler.fromSessao(
            obterSessaoPresencaUseCase.execute(id, principal.userId),
            principal.authorities,
            OffsetDateTime.now(),
        )
    }

    @GetMapping("/{id}/attendance/host-session")
    @PreAuthorize("hasAuthority('event.host')")
    @Operation(summary = "Sessão de operação do anfitrião (PIN em claro)")
    fun sessaoHost(@PathVariable id: UUID, authentication: Authentication): HostSessaoResponse {
        val principal = principal(authentication)
        return assembler.fromHost(
            obterSessaoHostUseCase.execute(id, principal.userId),
            principal.authorities,
            principal.userId,
            OffsetDateTime.now(),
        )
    }

    @PostMapping("/{id}/attendance/windows/entry")
    @PreAuthorize("hasAuthority('event.host')")
    @Operation(summary = "Abrir/renovar janela de entrada SECRET_SINGLE")
    fun abrirJanelaEntrada(@PathVariable id: UUID, authentication: Authentication): HostSessaoResponse {
        val principal = principal(authentication)
        return assembler.fromHost(
            abrirJanelaEntradaUseCase.execute(id, principal.userId),
            principal.authorities,
            principal.userId,
            OffsetDateTime.now(),
        )
    }

    @PostMapping("/{id}/attendance/windows/exit")
    @PreAuthorize("hasAuthority('event.host')")
    @Operation(summary = "Saída não exercitada neste sprint")
    fun abrirJanelaSaida(@PathVariable id: UUID): HostSessaoResponse {
        throw ConflitoEstadoException("Abertura de janela indisponível para este modo de presença.")
    }

    @PostMapping("/{id}/encerrar")
    @PreAuthorize("hasAuthority('event.host')")
    @Operation(summary = "Encerrar evento")
    fun encerrar(@PathVariable id: UUID, authentication: Authentication): HostSessaoResponse {
        val principal = principal(authentication)
        return assembler.fromHost(
            encerrarEventoUseCase.execute(id, principal.userId),
            principal.authorities,
            principal.userId,
            OffsetDateTime.now(),
        )
    }

    @PostMapping("/{id}/attendance/confirm")
    @PreAuthorize("hasAuthority('attendance.check_in')")
    @Operation(summary = "Confirmar presença (SECRET_SINGLE neste sprint)")
    fun confirmar(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ConfirmarPresencaRequest,
        authentication: Authentication,
    ): SessaoPresencaResponse {
        val principal = principal(authentication)
        return assembler.fromSessao(
            confirmarPresencaUseCase.execute(
                id,
                principal.userId,
                request.pin,
                request.deviceUuid,
                request.fase,
            ),
            principal.authorities,
            OffsetDateTime.now(),
        )
    }

    private fun garantirLeitura(evento: Evento, principal: IamPrincipal) {
        if (PresencaAssembler.AUTHORITY_VIEW in principal.authorities) {
            return
        }
        if (PresencaAssembler.AUTHORITY_MANAGE in principal.authorities && evento.eAnfitriao(principal.userId)) {
            return
        }
        throw AcessoNegadoException("Você não pode acessar este evento.")
    }

    private fun fases(eventoId: UUID, usuarioId: UUID): List<FasePresenca> =
        presencaRepository.findByEventoAndUsuario(eventoId, usuarioId).map { it.fase }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
