package br.ufpr.sept.so2.modules.presenca.api

import br.ufpr.sept.so2.modules.academico.application.ports.CursoEscopoPort
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.modules.presenca.api.dto.AtualizarEventoRequest
import br.ufpr.sept.so2.modules.presenca.api.dto.ConfirmarPresencaRequest
import br.ufpr.sept.so2.modules.presenca.api.dto.CriarEventoRequest
import br.ufpr.sept.so2.modules.presenca.api.dto.EventoResponse
import br.ufpr.sept.so2.modules.presenca.api.dto.HostSessaoResponse
import br.ufpr.sept.so2.modules.presenca.api.dto.SessaoPresencaResponse
import br.ufpr.sept.so2.modules.presenca.application.AbrirJanelaUseCase
import br.ufpr.sept.so2.modules.presenca.application.AtualizarEventoUseCase
import br.ufpr.sept.so2.modules.presenca.application.ConfirmarPresencaUseCase
import br.ufpr.sept.so2.modules.presenca.application.CriarEventoUseCase
import br.ufpr.sept.so2.modules.presenca.application.EncerrarEventoUseCase
import br.ufpr.sept.so2.modules.presenca.application.EventoAcesso
import br.ufpr.sept.so2.modules.presenca.application.ExcluirEventoUseCase
import br.ufpr.sept.so2.modules.presenca.application.ListarEventosDaSecretariaUseCase
import br.ufpr.sept.so2.modules.presenca.application.ListarEventosDoAlunoUseCase
import br.ufpr.sept.so2.modules.presenca.application.ListarEventosDoAnfitriaoUseCase
import br.ufpr.sept.so2.modules.presenca.application.ObterEventoUseCase
import br.ufpr.sept.so2.modules.presenca.application.ObterSessaoHostUseCase
import br.ufpr.sept.so2.modules.presenca.application.ObterSessaoPresencaUseCase
import br.ufpr.sept.so2.modules.presenca.application.ports.CursoSiglaPort
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.shared.api.PageResponse
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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
@Tag(name = "Eventos e presença", description = "Proof of Stay v4.1 (RF-F1-009 / RF-F3-002 / RF-F5-008)")
class EventoController(
    private val listarEventosDoAlunoUseCase: ListarEventosDoAlunoUseCase,
    private val listarEventosDoAnfitriaoUseCase: ListarEventosDoAnfitriaoUseCase,
    private val listarEventosDaSecretariaUseCase: ListarEventosDaSecretariaUseCase,
    private val criarEventoUseCase: CriarEventoUseCase,
    private val atualizarEventoUseCase: AtualizarEventoUseCase,
    private val excluirEventoUseCase: ExcluirEventoUseCase,
    private val obterEventoUseCase: ObterEventoUseCase,
    private val obterSessaoPresencaUseCase: ObterSessaoPresencaUseCase,
    private val obterSessaoHostUseCase: ObterSessaoHostUseCase,
    private val abrirJanelaUseCase: AbrirJanelaUseCase,
    private val encerrarEventoUseCase: EncerrarEventoUseCase,
    private val confirmarPresencaUseCase: ConfirmarPresencaUseCase,
    private val presencaRepository: PresencaRepository,
    private val cursoSiglaPort: CursoSiglaPort,
    private val cursoEscopoPort: CursoEscopoPort,
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

    /**
     * F5.14: mesma coleção `/events`, recortada pelos cursos da secretaria.
     * Não nasce outro recurso para o evento da secretaria.
     */
    @GetMapping(params = ["escopo=cursos"])
    @PreAuthorize("hasAuthority('event.manage')")
    @Operation(summary = "Listar eventos dos cursos da secretaria")
    fun listarPorCurso(
        @RequestParam(required = false) cursoId: UUID?,
        @RequestParam(required = false) estado: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<EventoResponse> {
        val principal = principal(authentication)
        val agora = OffsetDateTime.now()
        val pagina = listarEventosDaSecretariaUseCase.execute(principal.userId, cursoId, estado, pageable)
        val siglas = cursoSiglaPort.siglasPorId(pagina.content.mapNotNull { it.idCurso })
        val novo = if (PresencaAssembler.AUTHORITY_MANAGE in principal.authorities) {
            mapOf("novoEvento" to "/events")
        } else {
            emptyMap()
        }
        return PageResponse.ofWithLinks(
            pagina,
            Function { evento ->
                assembler.fromEvento(
                    evento,
                    emptyList(),
                    principal.authorities,
                    principal.userId,
                    agora,
                    siglas[evento.idCurso],
                )
            },
            novo,
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
    @Operation(summary = "Criar evento (QR|SECRET × SINGLE|DUAL)")
    fun criar(@Valid @RequestBody request: CriarEventoRequest, authentication: Authentication): EventoResponse {
        val principal = principal(authentication)
        val criado = criarEventoUseCase.execute(
            principal.userId,
            request.titulo,
            request.inicioEm,
            request.fimEm,
            request.cargaHoraria,
            request.attendanceMode,
            request.cursoId,
            principal.authorities,
        )
        return assembler.fromEvento(criado, emptyList(), principal.authorities, principal.userId, OffsetDateTime.now())
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('event.manage')")
    @Operation(summary = "Atualizar evento AGENDADO ou EM_ANDAMENTO")
    fun atualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AtualizarEventoRequest,
        authentication: Authentication,
    ): EventoResponse {
        val principal = principal(authentication)
        val atualizado = atualizarEventoUseCase.execute(
            id,
            principal.userId,
            principal.authorities,
            request.titulo,
            request.inicioEm,
            request.fimEm,
            request.cargaHoraria,
            request.attendanceMode,
            request.cursoId,
        )
        return assembler.fromEvento(
            atualizado,
            emptyList(),
            principal.authorities,
            principal.userId,
            OffsetDateTime.now(),
        )
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('event.manage')")
    @Operation(summary = "Excluir evento AGENDADO sem presença")
    fun excluir(@PathVariable id: UUID, authentication: Authentication): ResponseEntity<Void> {
        val principal = principal(authentication)
        excluirEventoUseCase.execute(id, principal.userId, principal.authorities)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('attendance.view_open','event.manage','event.host')")
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
    @Operation(summary = "Sessão de operação do anfitrião (PIN/token em claro)")
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
    @Operation(summary = "Abrir/renovar janela de entrada")
    fun abrirJanelaEntrada(
        @PathVariable id: UUID,
        authentication: Authentication,
        http: HttpServletRequest,
    ): HostSessaoResponse {
        val principal = principal(authentication)
        return assembler.fromHost(
            abrirJanelaUseCase.execute(id, principal.userId, FasePresenca.ENTRADA, clientIp(http)),
            principal.authorities,
            principal.userId,
            OffsetDateTime.now(),
        )
    }

    @PostMapping("/{id}/attendance/windows/exit")
    @PreAuthorize("hasAuthority('event.host')")
    @Operation(summary = "Abrir janela de saída (modos DUAL)")
    fun abrirJanelaSaida(
        @PathVariable id: UUID,
        authentication: Authentication,
        http: HttpServletRequest,
    ): HostSessaoResponse {
        val principal = principal(authentication)
        return assembler.fromHost(
            abrirJanelaUseCase.execute(id, principal.userId, FasePresenca.SAIDA, clientIp(http)),
            principal.authorities,
            principal.userId,
            OffsetDateTime.now(),
        )
    }

    @PostMapping("/{id}/attendance/qr/renew")
    @PreAuthorize("hasAuthority('event.host')")
    @Operation(summary = "Renovar token QR da janela ativa")
    fun renovarQr(
        @PathVariable id: UUID,
        authentication: Authentication,
        http: HttpServletRequest,
    ): HostSessaoResponse {
        val principal = principal(authentication)
        return assembler.fromHost(
            abrirJanelaUseCase.renovarQr(id, principal.userId, clientIp(http)),
            principal.authorities,
            principal.userId,
            OffsetDateTime.now(),
        )
    }

    @PostMapping("/{id}/encerrar")
    @PreAuthorize("hasAuthority('event.host')")
    @Operation(summary = "Encerrar evento")
    fun encerrar(
        @PathVariable id: UUID,
        authentication: Authentication,
        http: HttpServletRequest,
    ): HostSessaoResponse {
        val principal = principal(authentication)
        return assembler.fromHost(
            encerrarEventoUseCase.execute(id, principal.userId, clientIp(http)),
            principal.authorities,
            principal.userId,
            OffsetDateTime.now(),
        )
    }

    @PostMapping("/{id}/attendance/confirm")
    @PreAuthorize("hasAuthority('attendance.check_in')")
    @Operation(summary = "Confirmar presença (pin ou token conforme o modo)")
    fun confirmar(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ConfirmarPresencaRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): SessaoPresencaResponse {
        val principal = principal(authentication)
        return assembler.fromSessao(
            confirmarPresencaUseCase.execute(
                id,
                principal.userId,
                request.pin,
                request.token,
                request.deviceUuid,
                request.fase,
                clientIp(http),
            ),
            principal.authorities,
            OffsetDateTime.now(),
        )
    }

    private fun garantirLeitura(evento: Evento, principal: IamPrincipal) {
        if (PresencaAssembler.AUTHORITY_VIEW in principal.authorities) {
            return
        }
        if (EventoAcesso.alcanca(evento, principal.userId, principal.authorities, cursoEscopoPort)) {
            return
        }
        throw AcessoNegadoException("Você não pode acessar este evento.")
    }

    private fun fases(eventoId: UUID, usuarioId: UUID): List<FasePresenca> =
        presencaRepository.findByEventoAndUsuario(eventoId, usuarioId).map { it.fase }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal

        private fun clientIp(request: HttpServletRequest): String {
            val forwarded = request.getHeader("X-Forwarded-For")
            if (!forwarded.isNullOrBlank()) {
                return forwarded.split(",")[0].trim()
            }
            return request.remoteAddr
        }
    }
}
