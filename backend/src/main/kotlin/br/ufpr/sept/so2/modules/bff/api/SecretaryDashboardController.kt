package br.ufpr.sept.so2.modules.bff.api

import br.ufpr.sept.so2.modules.bff.api.dto.SecretaryDashboardResponse
import br.ufpr.sept.so2.modules.bff.application.SecretaryDashboardApplicationService
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
@Tag(name = "BFF", description = "Agregação de dashboard por perfil (RF-F5-001 / RF-TR-006)")
class SecretaryDashboardController(
    private val secretaryDashboardApplicationService: SecretaryDashboardApplicationService,
) {

    @GetMapping("/secretary")
    @PreAuthorize("hasAuthority('dashboard.view_secretary')")
    @Operation(summary = "Dashboard operacional da secretaria")
    fun secretary(authentication: Authentication): SecretaryDashboardResponse {
        val principal = authentication.principal as IamPrincipal
        return secretaryDashboardApplicationService.execute(principal.userId, principal.authorities)
    }
}
