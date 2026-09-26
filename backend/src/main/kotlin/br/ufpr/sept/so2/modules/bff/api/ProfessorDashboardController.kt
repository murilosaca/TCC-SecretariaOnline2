package br.ufpr.sept.so2.modules.bff.api

import br.ufpr.sept.so2.modules.bff.api.dto.ProfessorDashboardResponse
import br.ufpr.sept.so2.modules.bff.application.ProfessorDashboardApplicationService
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/bff/dashboard")
@Tag(name = "BFF", description = "Agregação de dashboard por perfil (RF-F1-001 / RF-F3-001 / RF-TR-006)")
class ProfessorDashboardController(
    private val professorDashboardApplicationService: ProfessorDashboardApplicationService,
) {

    @GetMapping("/professor")
    @PreAuthorize("hasAuthority('dashboard.view_self_professor')")
    @Operation(summary = "Dashboard unificado do professor")
    fun professor(authentication: Authentication): ProfessorDashboardResponse {
        val principal = authentication.principal as IamPrincipal
        return professorDashboardApplicationService.execute(principal.userId, principal.authorities)
    }
}
