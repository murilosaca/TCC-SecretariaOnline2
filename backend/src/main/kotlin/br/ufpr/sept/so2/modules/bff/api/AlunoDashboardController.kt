package br.ufpr.sept.so2.modules.bff.api

import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse
import br.ufpr.sept.so2.modules.bff.application.AlunoDashboardApplicationService
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/bff/dashboard")
@Tag(name = "BFF", description = "Agregação de dashboard por perfil (RF-F1-001 / RF-TR-006)")
class AlunoDashboardController(
    private val alunoDashboardApplicationService: AlunoDashboardApplicationService,
) {

    @GetMapping("/aluno")
    @PreAuthorize("hasAuthority('dashboard.view_own')")
    @Operation(summary = "Dashboard unificado do aluno")
    fun aluno(authentication: Authentication): AlunoDashboardResponse {
        val principal = authentication.principal as IamPrincipal
        if (!ePainelAluno(principal.authorities)) {
            throw AcessoNegadoException("Dashboard do aluno indisponível para esta sessão.")
        }
        return alunoDashboardApplicationService.execute(principal.userId, principal.authorities)
    }

    companion object {
        private fun ePainelAluno(authorities: List<String>?): Boolean =
            authorities != null && (
                authorities.contains("attendance.view_open") ||
                    authorities.contains("request.view_own")
                )
    }
}
