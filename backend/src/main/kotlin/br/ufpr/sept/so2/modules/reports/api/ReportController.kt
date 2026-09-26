package br.ufpr.sept.so2.modules.reports.api

import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.modules.reports.api.dto.CoordinatorReportResponse
import br.ufpr.sept.so2.modules.reports.api.dto.SecretaryReportResponse
import br.ufpr.sept.so2.modules.reports.application.ObterRelatorioCoordenadorUseCase
import br.ufpr.sept.so2.modules.reports.application.ObterRelatorioSecretariaUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/reports")
@Tag(name = "Relatórios", description = "Agregações analíticas (RF-F6-002 / RF-F5-011)")
class ReportController(
    private val obterRelatorioCoordenadorUseCase: ObterRelatorioCoordenadorUseCase,
    private val obterRelatorioSecretariaUseCase: ObterRelatorioSecretariaUseCase,
) {
    @GetMapping("/coordinator")
    @PreAuthorize("hasAuthority('report.view_coordinator')")
    @Operation(summary = "Relatório analítico do coordenador de curso")
    fun coordinator(
        authentication: Authentication,
        @RequestParam(required = false) periodo: String?,
        @RequestParam(required = false) curso: String?,
    ): CoordinatorReportResponse {
        val principal = authentication.principal as IamPrincipal
        val relatorio = obterRelatorioCoordenadorUseCase.execute(
            principal.userId,
            principal.authorities,
            periodo,
            curso,
        )
        val selfQuery = RelatorioCoordenadorAssembler.selfQuery(
            periodo ?: relatorio.periodoCodigo,
            curso ?: relatorio.cursoSigla,
        )
        return CoordinatorReportResponse.from(
            relatorio,
            RelatorioCoordenadorAssembler.links(relatorio, principal.authorities, selfQuery),
        )
    }

    @GetMapping("/secretary")
    @PreAuthorize("hasAuthority('report.view_secretary')")
    @Operation(summary = "Estatísticas operacionais da secretaria")
    fun secretary(
        authentication: Authentication,
        @RequestParam(required = false) periodo: String?,
        @RequestParam(required = false) curso: String?,
    ): SecretaryReportResponse {
        val principal = authentication.principal as IamPrincipal
        val relatorio = obterRelatorioSecretariaUseCase.execute(
            principal.userId,
            principal.authorities,
            periodo,
            curso,
        )
        val selfQuery = RelatorioSecretariaAssembler.selfQuery(
            periodo ?: relatorio.periodoCodigo,
            curso ?: relatorio.cursoSigla,
        )
        return SecretaryReportResponse.from(
            relatorio,
            RelatorioSecretariaAssembler.links(principal.authorities, selfQuery),
        )
    }
}
