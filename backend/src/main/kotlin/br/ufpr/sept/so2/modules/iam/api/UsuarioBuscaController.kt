package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.api.dto.UsuarioOpcaoResponse
import br.ufpr.sept.so2.modules.iam.application.AdminUsuarioApplicationService
import br.ufpr.sept.so2.shared.api.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.function.Function

/**
 * Busca enxuta para o picker reusável. Secretaria com course.manage usa este endpoint;
 * GET /admin/usuarios permanece exclusivo de user.manage_all.
 */
@RestController
@RequestMapping("/iam/usuarios")
@Tag(name = "IAM · Usuários (picker)", description = "Busca de usuários para vínculos (F5.7+)")
class UsuarioBuscaController(
    private val adminUsuarioApplicationService: AdminUsuarioApplicationService,
) {
    @GetMapping
    @PreAuthorize("hasAnyAuthority('user.manage_all', 'course.manage')")
    @Operation(summary = "Buscar usuários para picker (nome, e-mail, GRR)")
    fun buscar(
        @RequestParam(required = false) q: String?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): PageResponse<UsuarioOpcaoResponse> =
        PageResponse.ofWithLinks(
            adminUsuarioApplicationService.listar(q, pageable),
            Function { UsuarioOpcaoResponse.from(it) },
        )
}
