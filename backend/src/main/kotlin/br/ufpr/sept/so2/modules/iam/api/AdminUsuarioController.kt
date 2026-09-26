package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.api.dto.AdminUsuarioRequest
import br.ufpr.sept.so2.modules.iam.api.dto.AdminUsuarioResponse
import br.ufpr.sept.so2.modules.iam.application.AdminUsuarioApplicationService
import br.ufpr.sept.so2.modules.iam.application.AtualizarUsuarioAdminUseCase
import br.ufpr.sept.so2.modules.iam.application.CriarUsuarioAdminUseCase
import br.ufpr.sept.so2.modules.iam.application.DesativarUsuarioAdminUseCase
import br.ufpr.sept.so2.modules.iam.application.ResetSenhaAdminUseCase
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/admin/usuarios")
@Tag(name = "Admin · Usuários", description = "F7.1 / F7.8 — gerenciar usuários e reset por link")
class AdminUsuarioController(
    private val adminUsuarioApplicationService: AdminUsuarioApplicationService,
    private val criarUsuarioAdminUseCase: CriarUsuarioAdminUseCase,
    private val atualizarUsuarioAdminUseCase: AtualizarUsuarioAdminUseCase,
    private val desativarUsuarioAdminUseCase: DesativarUsuarioAdminUseCase,
    private val resetSenhaAdminUseCase: ResetSenhaAdminUseCase,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('user.manage_all')")
    @Operation(summary = "Listar usuários (busca por nome, e-mail e GRR)")
    fun listar(
        @RequestParam(required = false) q: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<AdminUsuarioResponse> {
        val principal = principal(authentication)
        val podeReset = "user.reset_password" in principal.authorities
        return PageResponse.ofWithLinks(
            adminUsuarioApplicationService.listar(q, pageable),
            Function { usuario -> AdminUsuarioResponse.from(usuario, true, podeReset) },
            mapOf("criar" to "/admin/usuarios"),
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user.manage_all')")
    @Operation(summary = "Buscar usuário por id")
    fun buscar(@PathVariable id: UUID, authentication: Authentication): AdminUsuarioResponse {
        val principal = principal(authentication)
        val usuario = adminUsuarioApplicationService.buscar(id)
        return AdminUsuarioResponse.from(usuario, true, "user.reset_password" in principal.authorities)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('user.manage_all')")
    @Operation(summary = "Criar usuário (senhaAlterada=false; admin não recebe a senha)")
    fun criar(
        @Valid @RequestBody request: AdminUsuarioRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): AdminUsuarioResponse {
        val principal = principal(authentication)
        val criado = criarUsuarioAdminUseCase.execute(
            principal.userId,
            request.nome!!,
            request.emailInstitucional!!,
            request.emailPessoal,
            request.grr,
            http.remoteAddr,
        )
        return AdminUsuarioResponse.from(criado, true, "user.reset_password" in principal.authorities)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.manage_all')")
    @Operation(summary = "Editar usuário")
    fun atualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AdminUsuarioRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): AdminUsuarioResponse {
        val principal = principal(authentication)
        val atualizado = atualizarUsuarioAdminUseCase.execute(
            principal.userId,
            id,
            request.nome!!,
            request.emailInstitucional!!,
            request.emailPessoal,
            request.grr,
            http.remoteAddr,
        )
        return AdminUsuarioResponse.from(atualizado, true, "user.reset_password" in principal.authorities)
    }

    @PostMapping("/{id}/desativar")
    @PreAuthorize("hasAuthority('user.manage_all')")
    @Operation(summary = "Desativar usuário (soft)")
    fun desativar(
        @PathVariable id: UUID,
        authentication: Authentication,
        http: HttpServletRequest,
    ): AdminUsuarioResponse {
        val principal = principal(authentication)
        val desativado = desativarUsuarioAdminUseCase.execute(principal.userId, id, http.remoteAddr)
        return AdminUsuarioResponse.from(desativado, true, "user.reset_password" in principal.authorities)
    }

    @PostMapping("/{id}/reset-senha")
    @PreAuthorize("hasAuthority('user.reset_password')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Disparar reset por link JWT 1-uso (F7.8)")
    fun resetSenha(
        @PathVariable id: UUID,
        authentication: Authentication,
        http: HttpServletRequest,
    ) {
        resetSenhaAdminUseCase.execute(principal(authentication).userId, id, http.remoteAddr)
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
