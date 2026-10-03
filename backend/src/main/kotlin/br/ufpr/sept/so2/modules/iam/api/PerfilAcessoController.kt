package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.api.dto.PerfilRequest
import br.ufpr.sept.so2.modules.iam.api.dto.PerfilAcessoResponse
import br.ufpr.sept.so2.modules.iam.application.ports.PerfilAcessoPort
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
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
@RequestMapping("/admin/perfis")
@Tag(name = "Admin · Perfis", description = "F7.2 — perfis agregadores de authorities")
class PerfilAcessoController(
    private val perfilAcessoPort: PerfilAcessoPort,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('iam.manage_roles')")
    @Operation(summary = "Listar perfis")
    fun listar(
        @RequestParam(required = false) q: String?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): PageResponse<PerfilAcessoResponse> =
        PageResponse.ofWithLinks(
            perfilAcessoPort.listar(q, pageable),
            Function { PerfilAcessoResponse.from(it) },
            mapOf("criar" to "/admin/perfis"),
        )

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('iam.manage_roles')")
    @Operation(summary = "Buscar perfil")
    fun buscar(@PathVariable id: UUID): PerfilAcessoResponse =
        PerfilAcessoResponse.from(perfilAcessoPort.buscar(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('iam.manage_roles')")
    @Operation(summary = "Criar perfil customizado")
    fun criar(
        @RequestBody request: PerfilRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): PerfilAcessoResponse {
        val nome = request.nome ?: throw br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException("Nome é obrigatório.")
        val criado = perfilAcessoPort.criar(
            nome,
            request.descricao,
            request.authorities,
            principal(authentication).userId,
            http.remoteAddr,
        )
        return PerfilAcessoResponse.from(criado)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('iam.manage_roles')")
    @Operation(summary = "Atualizar descrição e authorities do perfil")
    fun atualizar(
        @PathVariable id: UUID,
        @RequestBody request: PerfilRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): PerfilAcessoResponse =
        PerfilAcessoResponse.from(
            perfilAcessoPort.atualizar(
                id,
                request.descricao,
                request.authorities,
                principal(authentication).userId,
                http.remoteAddr,
            ),
        )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('iam.manage_roles')")
    @Operation(summary = "Excluir perfil customizado sem usuários ativos")
    fun excluir(
        @PathVariable id: UUID,
        authentication: Authentication,
        http: HttpServletRequest,
    ) {
        perfilAcessoPort.excluir(id, principal(authentication).userId, http.remoteAddr)
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
