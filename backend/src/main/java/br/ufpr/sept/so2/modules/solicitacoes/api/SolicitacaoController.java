package br.ufpr.sept.so2.modules.solicitacoes.api;

import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal;
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.CriarSolicitacaoRequest;
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.SolicitacaoResponse;
import br.ufpr.sept.so2.modules.solicitacoes.application.CriarSolicitacaoUseCase;
import br.ufpr.sept.so2.modules.solicitacoes.application.ListarMinhasSolicitacoesUseCase;
import br.ufpr.sept.so2.modules.solicitacoes.application.ObterSolicitacaoUseCase;
import br.ufpr.sept.so2.shared.api.PageResponse;
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/requests")
@Tag(name = "Solicitações", description = "Motor genérico de requerimentos (RF-F1-005 / RF-TR-001)")
public class SolicitacaoController {

    private final ListarMinhasSolicitacoesUseCase listarMinhasSolicitacoesUseCase;
    private final CriarSolicitacaoUseCase criarSolicitacaoUseCase;
    private final ObterSolicitacaoUseCase obterSolicitacaoUseCase;
    private final SolicitacaoAssembler assembler;

    public SolicitacaoController(
            ListarMinhasSolicitacoesUseCase listarMinhasSolicitacoesUseCase,
            CriarSolicitacaoUseCase criarSolicitacaoUseCase,
            ObterSolicitacaoUseCase obterSolicitacaoUseCase,
            SolicitacaoAssembler assembler
    ) {
        this.listarMinhasSolicitacoesUseCase = listarMinhasSolicitacoesUseCase;
        this.criarSolicitacaoUseCase = criarSolicitacaoUseCase;
        this.obterSolicitacaoUseCase = obterSolicitacaoUseCase;
        this.assembler = assembler;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('request.view_own')")
    @Operation(summary = "Listar minhas solicitações")
    public PageResponse<SolicitacaoResponse> listar(
            @RequestParam(defaultValue = "me") String solicitante,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer ano,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        if (!"me".equalsIgnoreCase(solicitante)) {
            throw new AcessoNegadoException("Neste momento só é possível listar as próprias solicitações.");
        }
        IamPrincipal principal = principal(authentication);
        return PageResponse.ofWithLinks(
                listarMinhasSolicitacoesUseCase.execute(principal.userId(), estado, tipo, ano, pageable),
                item -> assembler.from(item, principal.authorities(), false),
                principal.authorities().contains("request.open")
                        ? Map.of("novaSolicitacao", "/request-types")
                        : Map.of()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('request.open')")
    @Operation(summary = "Abrir solicitação a partir do form_schema")
    public SolicitacaoResponse criar(
            @Valid @RequestBody CriarSolicitacaoRequest request,
            Authentication authentication,
            HttpServletRequest http
    ) {
        IamPrincipal principal = principal(authentication);
        return assembler.from(
                criarSolicitacaoUseCase.execute(
                        principal.userId(),
                        request.tipoCodigo(),
                        request.payload(),
                        clientIp(http)
                ),
                principal.authorities(),
                true
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('request.view_own')")
    @Operation(summary = "Detalhe com timeline (mais recente no topo)")
    public SolicitacaoResponse buscar(@PathVariable UUID id, Authentication authentication) {
        IamPrincipal principal = principal(authentication);
        return assembler.from(
                obterSolicitacaoUseCase.execute(id, principal.userId()),
                principal.authorities(),
                true
        );
    }

    private static IamPrincipal principal(Authentication authentication) {
        return (IamPrincipal) authentication.getPrincipal();
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
