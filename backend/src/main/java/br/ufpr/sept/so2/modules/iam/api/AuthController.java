package br.ufpr.sept.so2.modules.iam.api;

import br.ufpr.sept.so2.modules.iam.api.dto.LoginRequest;
import br.ufpr.sept.so2.modules.iam.api.dto.LoginResponse;
import br.ufpr.sept.so2.modules.iam.api.dto.PrimeiroAcessoRequest;
import br.ufpr.sept.so2.modules.iam.api.dto.RecuperarSenhaRequest;
import br.ufpr.sept.so2.modules.iam.api.dto.RedefinirSenhaRequest;
import br.ufpr.sept.so2.modules.iam.api.dto.SessaoResponse;
import br.ufpr.sept.so2.modules.iam.application.ConsultarSessaoUseCase;
import br.ufpr.sept.so2.modules.iam.application.LoginResult;
import br.ufpr.sept.so2.modules.iam.application.LoginUseCase;
import br.ufpr.sept.so2.modules.iam.application.LogoutUseCase;
import br.ufpr.sept.so2.modules.iam.application.PrimeiroAcessoUseCase;
import br.ufpr.sept.so2.modules.iam.application.RecuperarSenhaUseCase;
import br.ufpr.sept.so2.modules.iam.application.RedefinirSenhaUseCase;
import br.ufpr.sept.so2.modules.iam.application.RefreshTokenUseCase;
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal;
import br.ufpr.sept.so2.modules.iam.infrastructure.security.RefreshCookieFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "IAM — login, refresh, recuperação e primeiro acesso")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RecuperarSenhaUseCase recuperarSenhaUseCase;
    private final RedefinirSenhaUseCase redefinirSenhaUseCase;
    private final PrimeiroAcessoUseCase primeiroAcessoUseCase;
    private final ConsultarSessaoUseCase consultarSessaoUseCase;
    private final RefreshCookieFactory refreshCookieFactory;

    public AuthController(
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase,
            RecuperarSenhaUseCase recuperarSenhaUseCase,
            RedefinirSenhaUseCase redefinirSenhaUseCase,
            PrimeiroAcessoUseCase primeiroAcessoUseCase,
            ConsultarSessaoUseCase consultarSessaoUseCase,
            RefreshCookieFactory refreshCookieFactory
    ) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.recuperarSenhaUseCase = recuperarSenhaUseCase;
        this.redefinirSenhaUseCase = redefinirSenhaUseCase;
        this.primeiroAcessoUseCase = primeiroAcessoUseCase;
        this.consultarSessaoUseCase = consultarSessaoUseCase;
        this.refreshCookieFactory = refreshCookieFactory;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar (RF-F0-001)")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest http
    ) {
        LoginResult result = loginUseCase.execute(request.identificador(), request.senha(), clientIp(http));
        return withRefreshCookie(result);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotacionar refresh token (RNF-SEC-03)")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = "${app.iam.cookie-name:so2_refresh}", required = false) String refreshToken,
            HttpServletRequest http
    ) {
        LoginResult result = refreshTokenUseCase.execute(refreshToken, clientIp(http));
        return withRefreshCookie(result);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerrar a sessão corrente")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "${app.iam.cookie-name:so2_refresh}", required = false) String refreshToken
    ) {
        logoutUseCase.execute(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.clear().toString())
                .build();
    }

    @PostMapping("/recuperar-senha")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Solicitar link de recuperação (RF-F0-002)")
    public void recuperarSenha(
            @Valid @RequestBody RecuperarSenhaRequest request,
            HttpServletRequest http
    ) {
        recuperarSenhaUseCase.execute(request.email(), clientIp(http));
    }

    @GetMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Validar token de redefinição sem consumi-lo")
    public void validarToken(@RequestParam String token) {
        redefinirSenhaUseCase.validarToken(token);
    }

    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Redefinir senha via token de uso único (RF-F0-003)")
    public void redefinirSenha(
            @Valid @RequestBody RedefinirSenhaRequest request,
            HttpServletRequest http
    ) {
        redefinirSenhaUseCase.execute(request.token(), request.novaSenha(), clientIp(http));
    }

    @PostMapping("/primeiro-acesso")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Concluir primeiro acesso (RF-F1-002)")
    public void primeiroAcesso(
            @Valid @RequestBody PrimeiroAcessoRequest request,
            Authentication authentication,
            HttpServletRequest http
    ) {
        IamPrincipal principal = (IamPrincipal) authentication.getPrincipal();
        primeiroAcessoUseCase.execute(
                principal.userId(),
                request.novaSenha(),
                Boolean.TRUE.equals(request.aceiteTermos()),
                clientIp(http),
                http.getHeader(HttpHeaders.USER_AGENT)
        );
    }

    @GetMapping("/me")
    @Operation(summary = "Sessão atual (sem role; só authorities)")
    public SessaoResponse me(Authentication authentication) {
        IamPrincipal principal = (IamPrincipal) authentication.getPrincipal();
        return SessaoResponse.from(consultarSessaoUseCase.execute(principal.userId()));
    }

    private ResponseEntity<LoginResponse> withRefreshCookie(LoginResult result) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.create(result.refreshToken()).toString())
                .body(LoginResponse.from(result));
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
