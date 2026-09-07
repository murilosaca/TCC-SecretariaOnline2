package br.ufpr.sept.so2.modules.bff.api;

import br.ufpr.sept.so2.modules.bff.api.dto.AlunoDashboardResponse;
import br.ufpr.sept.so2.modules.bff.application.AlunoDashboardApplicationService;
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal;
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bff/dashboard")
@Tag(name = "BFF", description = "Agregação de dashboard por perfil (RF-F1-001 / RF-TR-006)")
public class AlunoDashboardController {

    private final AlunoDashboardApplicationService alunoDashboardApplicationService;

    public AlunoDashboardController(AlunoDashboardApplicationService alunoDashboardApplicationService) {
        this.alunoDashboardApplicationService = alunoDashboardApplicationService;
    }

    @GetMapping("/aluno")
    @PreAuthorize("hasAuthority('dashboard.view_own')")
    @Operation(summary = "Dashboard unificado do aluno")
    public AlunoDashboardResponse aluno(Authentication authentication) {
        IamPrincipal principal = (IamPrincipal) authentication.getPrincipal();
        if (!ePainelAluno(principal.authorities())) {
            throw new AcessoNegadoException("Dashboard do aluno indisponível para esta sessão.");
        }
        return alunoDashboardApplicationService.execute(principal.userId(), principal.authorities());
    }

    private static boolean ePainelAluno(List<String> authorities) {
        return authorities != null && (
                authorities.contains("attendance.view_open")
                        || authorities.contains("request.view_own")
        );
    }
}
