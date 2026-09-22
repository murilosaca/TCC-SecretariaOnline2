package br.ufpr.sept.so2.modules.estagio.api

import br.ufpr.sept.so2.modules.estagio.api.dto.AtribuirEstagioRequest
import br.ufpr.sept.so2.modules.estagio.api.dto.CoePoolResponse
import br.ufpr.sept.so2.modules.estagio.application.AtribuirEstagioCoeUseCase
import br.ufpr.sept.so2.modules.estagio.application.ObterPoolCoeUseCase
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/comissoes/coe")
@Tag(name = "COE", description = "Pool da comissão de estágios — só atribuição (RF-F4-002)")
class CoeController(
    private val obterPoolCoeUseCase: ObterPoolCoeUseCase,
    private val atribuirEstagioCoeUseCase: AtribuirEstagioCoeUseCase,
    private val assembler: CoeAssembler,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('internship.review')")
    @Operation(summary = "KPIs e lista do pool COE (não atribuídos + comigo)")
    fun pool(authentication: Authentication): CoePoolResponse {
        val principal = principal(authentication)
        return assembler.from(obterPoolCoeUseCase.execute(principal.userId))
    }

    @PostMapping("/atribuicoes")
    @PreAuthorize("hasAuthority('internship.review')")
    @Operation(summary = "Atribuir um estágio a um membro do COE")
    fun atribuir(
        @RequestBody request: AtribuirEstagioRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): CoePoolResponse {
        val principal = principal(authentication)
        return assembler.from(
            atribuirEstagioCoeUseCase.execute(
                principal.userId,
                request.estagioId,
                request.assigneeId,
                clientIp(http),
            ),
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
