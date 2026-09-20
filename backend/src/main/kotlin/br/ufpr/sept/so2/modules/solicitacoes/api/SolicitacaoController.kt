package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.CriarSolicitacaoRequest
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.SolicitacaoResponse
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.TransicionarSolicitacaoRequest
import br.ufpr.sept.so2.modules.solicitacoes.application.CriarSolicitacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.DeliberarSolicitacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.ListarFilaDeliberacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.ListarMinhasSolicitacoesUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.ObterSolicitacaoUseCase
import br.ufpr.sept.so2.shared.api.PageResponse
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
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
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/requests")
@Tag(name = "Solicitações", description = "Motor genérico de requerimentos (RF-F1-005 / RF-F3-003)")
class SolicitacaoController(
    private val listarMinhasSolicitacoesUseCase: ListarMinhasSolicitacoesUseCase,
    private val listarFilaDeliberacaoUseCase: ListarFilaDeliberacaoUseCase,
    private val criarSolicitacaoUseCase: CriarSolicitacaoUseCase,
    private val obterSolicitacaoUseCase: ObterSolicitacaoUseCase,
    private val deliberarSolicitacaoUseCase: DeliberarSolicitacaoUseCase,
    private val assembler: SolicitacaoAssembler,
) {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('request.view_own','request.deliberate')")
    @Operation(summary = "Listar minhas solicitações ou a fila de deliberação")
    fun listar(
        @RequestParam(name = "canDeliberate", defaultValue = "false") canDeliberate: Boolean,
        @RequestParam(name = "solicitante", defaultValue = "me") solicitante: String,
        @RequestParam(name = "estado", required = false) estado: String?,
        @RequestParam(name = "tipo", required = false) tipo: String?,
        @RequestParam(name = "ano", required = false) ano: Int?,
        @RequestParam(name = "atraso", required = false) atraso: Boolean?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<SolicitacaoResponse> {
        val principal = principal(authentication)
        if (canDeliberate) {
            if (!principal.authorities.contains("request.deliberate")) {
                throw AcessoNegadoException("Você não tem permissão para esta operação.")
            }
            return PageResponse.ofWithLinks(
                listarFilaDeliberacaoUseCase.execute(tipo, atraso == true, pageable),
                Function { item -> assembler.from(item, principal.authorities, false) },
            )
        }
        if (!principal.authorities.contains("request.view_own")) {
            throw AcessoNegadoException("Você não tem permissão para esta operação.")
        }
        if (!"me".equals(solicitante, ignoreCase = true)) {
            throw AcessoNegadoException("Neste momento só é possível listar as próprias solicitações.")
        }
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
    @PreAuthorize("hasAnyAuthority('request.view_own','request.deliberate')")
    @Operation(summary = "Detalhe com timeline (mais recente no topo)")
    fun buscar(@PathVariable("id") id: UUID, authentication: Authentication): SolicitacaoResponse {
        val principal = principal(authentication)
        return assembler.from(
            obterSolicitacaoUseCase.execute(id, principal.userId, principal.authorities),
            principal.authorities,
            true,
        )
    }

    @PostMapping("/{id}/transitions")
    @PreAuthorize("hasAuthority('request.deliberate')")
    @Operation(summary = "Deliberar solicitação (DEFER / INDEFER / REQUEST_ADJUST)")
    fun transicionar(
        @PathVariable("id") id: UUID,
        @Valid @RequestBody request: TransicionarSolicitacaoRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): SolicitacaoResponse {
        val principal = principal(authentication)
        return assembler.from(
            deliberarSolicitacaoUseCase.execute(
                id,
                principal.userId,
                request.action,
                request.parecer,
                clientIp(http),
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
