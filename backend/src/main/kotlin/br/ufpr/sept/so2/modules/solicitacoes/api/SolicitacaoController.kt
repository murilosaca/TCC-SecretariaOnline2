package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.BulkAtribuirRequest
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.CriarSolicitacaoRequest
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.DeliberadorOpcaoResponse
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.SolicitacaoFilaPageResponse
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.SolicitacaoResponse
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.TransicionarSolicitacaoRequest
import br.ufpr.sept.so2.modules.solicitacoes.application.AtribuirDeliberadoresBulkUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.CriarSolicitacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.DeliberarSolicitacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.ListarFilaCursoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.ListarFilaDeliberacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.ListarMinhasSolicitacoesUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.ObterSolicitacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.SolicitacaoCursoEscopo
import br.ufpr.sept.so2.shared.api.PageResponse
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/requests")
@Tag(name = "Solicitações", description = "Motor genérico de requerimentos (RF-F1-005 / RF-F3-003 / RF-F5-002)")
class SolicitacaoController(
    private val listarMinhasSolicitacoesUseCase: ListarMinhasSolicitacoesUseCase,
    private val listarFilaDeliberacaoUseCase: ListarFilaDeliberacaoUseCase,
    private val listarFilaCursoUseCase: ListarFilaCursoUseCase,
    private val criarSolicitacaoUseCase: CriarSolicitacaoUseCase,
    private val obterSolicitacaoUseCase: ObterSolicitacaoUseCase,
    private val deliberarSolicitacaoUseCase: DeliberarSolicitacaoUseCase,
    private val atribuirDeliberadoresBulkUseCase: AtribuirDeliberadoresBulkUseCase,
    private val solicitacaoCursoEscopo: SolicitacaoCursoEscopo,
    private val usuarioRepository: UsuarioRepository,
    private val assembler: SolicitacaoAssembler,
) {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('request.view_own','request.deliberate','request.view_curso')")
    @Operation(summary = "Listar minhas, fila de deliberação ou fila central do curso")
    fun listar(
        @RequestParam(name = "canDeliberate", defaultValue = "false") canDeliberate: Boolean,
        @RequestParam(name = "solicitante", defaultValue = "me") solicitante: String,
        @RequestParam(name = "estado", required = false) estado: String?,
        @RequestParam(name = "tipo", required = false) tipo: String?,
        @RequestParam(name = "ano", required = false) ano: Int?,
        @RequestParam(name = "curso", required = false) curso: UUID?,
        @RequestParam(name = "atraso", required = false) atraso: Boolean?,
        @RequestParam(name = "slaBreached", required = false) slaBreached: Boolean?,
        @RequestParam(name = "format", required = false) format: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): Any {
        val principal = principal(authentication)
        val somenteAtraso = slaBreached == true || atraso == true

        if (canDeliberate) {
            val deliberante = principal.authorities.contains("request.deliberate")
            val viewCurso = principal.authorities.contains("request.view_curso")
            if (!deliberante && !viewCurso) {
                throw AcessoNegadoException("Você não tem permissão para esta operação.")
            }
            val filtroSolicitantes = if (viewCurso) {
                solicitacaoCursoEscopo.solicitanteIdsNoEscopo(principal.userId)
            } else {
                null
            }
            return PageResponse.ofWithLinks(
                listarFilaDeliberacaoUseCase.execute(tipo, somenteAtraso, pageable, filtroSolicitantes),
                Function { item -> assembler.from(item, principal.authorities, false) },
            )
        }

        if (principal.authorities.contains("request.view_own") &&
            "me".equals(solicitante, ignoreCase = true) &&
            !principal.authorities.contains("request.view_curso")
        ) {
            return PageResponse.ofWithLinks(
                listarMinhasSolicitacoesUseCase.execute(principal.userId, estado, tipo, ano, pageable),
                Function { item -> assembler.from(item, principal.authorities, false) },
                if (principal.authorities.contains("request.open")) {
                    mapOf("novaSolicitacao" to "/request-types")
                } else {
                    emptyMap()
                },
            )
        }

        if (principal.authorities.contains("request.view_own") &&
            "me".equals(solicitante, ignoreCase = true) &&
            format == null &&
            slaBreached != true &&
            curso == null &&
            atraso != true
        ) {
            // Aluno (ou sessão com view_own) pedindo as próprias — mesmo com view_curso excepcional.
            return PageResponse.ofWithLinks(
                listarMinhasSolicitacoesUseCase.execute(principal.userId, estado, tipo, ano, pageable),
                Function { item -> assembler.from(item, principal.authorities, false) },
                if (principal.authorities.contains("request.open")) {
                    mapOf("novaSolicitacao" to "/request-types")
                } else {
                    emptyMap()
                },
            )
        }

        if (!principal.authorities.contains("request.view_curso")) {
            throw AcessoNegadoException("Você não tem permissão para esta operação.")
        }

        val pagina = listarFilaCursoUseCase.execute(
            principal.userId,
            if (somenteAtraso && estado.isNullOrBlank()) "TODOS" else estado,
            tipo,
            curso,
            somenteAtraso,
            pageable,
        )
        val content = pagina.content.map { assembler.from(it, principal.authorities, false) }

        if ("csv".equals(format, ignoreCase = true)) {
            val nome = "atrasados-${LocalDate.now()}.csv"
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$nome\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(SolicitacaoCsvMarshaller.atrasados(content))
        }

        val pageResponse = PageResponse.ofWithLinks(
            pagina,
            Function { item -> assembler.from(item, principal.authorities, false) },
            mapOf("bulk" to "/requests/bulk"),
        )
        val deliberadores = usuarioRepository.findAtivosByAuthority("request.deliberate")
            .map { u ->
                DeliberadorOpcaoResponse(
                    u.id,
                    u.nome.ifBlank { u.emailInstitucional.value },
                )
            }
        return SolicitacaoFilaPageResponse(
            pageResponse.content,
            pageResponse.page,
            deliberadores,
            pageResponse.links,
        )
    }

    @PatchMapping("/bulk")
    @PreAuthorize("hasAnyAuthority('request.triage','request.deliberate')")
    @Operation(summary = "Atribuir deliberador em massa (F5.2)")
    fun bulkAtribuir(
        @Valid @RequestBody request: BulkAtribuirRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): List<SolicitacaoResponse> {
        val principal = principal(authentication)
        if (!principal.authorities.contains("request.view_curso") &&
            !principal.authorities.contains("request.triage")
        ) {
            throw AcessoNegadoException("Você não tem permissão para esta operação.")
        }
        return atribuirDeliberadoresBulkUseCase.execute(
            principal.userId,
            principal.authorities,
            request.ids,
            request.deliberadorId,
            clientIp(http),
        ).map { assembler.from(it, principal.authorities, false) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('request.open')")
    @Operation(summary = "Abrir solicitação a partir do form_schema")
    fun criar(
        @Valid @RequestBody request: CriarSolicitacaoRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): SolicitacaoResponse {
        val principal = principal(authentication)
        return assembler.from(
            criarSolicitacaoUseCase.execute(
                principal.userId,
                request.tipoCodigo,
                request.payload,
                clientIp(http),
            ),
            principal.authorities,
            true,
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('request.view_own','request.deliberate','request.view_curso')")
    @Operation(summary = "Detalhe com timeline (mais recente no topo)")
    fun buscar(
        @PathVariable("id") id: UUID,
        @RequestParam(name = "token", required = false) token: String?,
        authentication: Authentication,
    ): SolicitacaoResponse {
        val principal = principal(authentication)
        return assembler.from(
            obterSolicitacaoUseCase.execute(id, principal.userId, principal.authorities, token),
            principal.authorities,
            true,
        )
    }

    @PostMapping("/{id}/transitions")
    @PreAuthorize("hasAuthority('request.deliberate')")
    @Operation(summary = "Deliberar solicitação (DEFER / INDEFER / REQUEST_ADJUST)")
    fun transicionar(
        @PathVariable("id") id: UUID,
        @RequestParam(name = "token", required = false) token: String?,
        @Valid @RequestBody request: TransicionarSolicitacaoRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): SolicitacaoResponse {
        val principal = principal(authentication)
        return assembler.from(
            deliberarSolicitacaoUseCase.execute(
                id,
                principal.userId,
                principal.authorities,
                request.action,
                request.parecer,
                clientIp(http),
                token,
            ),
            principal.authorities,
            true,
        )
    }

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
