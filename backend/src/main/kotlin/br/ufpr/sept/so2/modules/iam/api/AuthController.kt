package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.api.dto.LoginRequest
import br.ufpr.sept.so2.modules.iam.api.dto.LoginResponse
import br.ufpr.sept.so2.modules.iam.api.dto.PrimeiroAcessoRequest
import br.ufpr.sept.so2.modules.iam.api.dto.RecuperarSenhaRequest
import br.ufpr.sept.so2.modules.iam.api.dto.RedefinirSenhaRequest
import br.ufpr.sept.so2.modules.iam.api.dto.SessaoResponse
import br.ufpr.sept.so2.modules.iam.application.ConsultarSessaoUseCase
import br.ufpr.sept.so2.modules.iam.application.LoginResult
import br.ufpr.sept.so2.modules.iam.application.LoginUseCase
import br.ufpr.sept.so2.modules.iam.application.LogoutUseCase
import br.ufpr.sept.so2.modules.iam.application.PrimeiroAcessoUseCase
import br.ufpr.sept.so2.modules.iam.application.RecuperarSenhaUseCase
import br.ufpr.sept.so2.modules.iam.application.RedefinirSenhaUseCase
import br.ufpr.sept.so2.modules.iam.application.RefreshTokenUseCase
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.modules.iam.infrastructure.security.RefreshCookieFactory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "IAM — login, refresh, recuperação e primeiro acesso")
class AuthController(
    private val loginUseCase: LoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val recuperarSenhaUseCase: RecuperarSenhaUseCase,
    private val redefinirSenhaUseCase: RedefinirSenhaUseCase,
    private val primeiroAcessoUseCase: PrimeiroAcessoUseCase,
    private val consultarSessaoUseCase: ConsultarSessaoUseCase,
    private val refreshCookieFactory: RefreshCookieFactory,
) {
    @PostMapping("/login")
    @Operation(summary = "Autenticar (RF-F0-001)")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        http: HttpServletRequest,
    ): ResponseEntity<LoginResponse> {
        val result = loginUseCase.execute(request.identificador, request.senha, clientIp(http))
        return withRefreshCookie(result)
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotacionar refresh token (RNF-SEC-03)")
    fun refresh(
        @CookieValue(name = "\${app.iam.cookie-name:so2_refresh}", required = false) refreshToken: String?,
        http: HttpServletRequest,
    ): ResponseEntity<LoginResponse> {
        val result = refreshTokenUseCase.execute(refreshToken, clientIp(http))
        return withRefreshCookie(result)
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerrar a sessão corrente")
    fun logout(
        @CookieValue(name = "\${app.iam.cookie-name:so2_refresh}", required = false) refreshToken: String?,
    ): ResponseEntity<Void> {
        logoutUseCase.execute(refreshToken)
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.clear().toString())
            .build()
    }

    @PostMapping("/recuperar-senha")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Solicitar link de recuperação (RF-F0-002)")
    fun recuperarSenha(
        @Valid @RequestBody request: RecuperarSenhaRequest,
        http: HttpServletRequest,
    ) {
        recuperarSenhaUseCase.execute(request.email, clientIp(http))
    }

    @GetMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Validar token de redefinição sem consumi-lo")
    fun validarToken(@RequestParam token: String) {
        redefinirSenhaUseCase.validarToken(token)
    }

    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Redefinir senha via token de uso único (RF-F0-003)")
    fun redefinirSenha(
        @Valid @RequestBody request: RedefinirSenhaRequest,
        http: HttpServletRequest,
    ) {
        redefinirSenhaUseCase.execute(request.token, request.novaSenha, clientIp(http))
    }

    @PostMapping("/primeiro-acesso")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Concluir primeiro acesso (RF-F1-002)")
    fun primeiroAcesso(
        @Valid @RequestBody request: PrimeiroAcessoRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ) {
        val principal = authentication.principal as IamPrincipal
        primeiroAcessoUseCase.execute(
            principal.userId,
            request.novaSenha,
            request.aceiteTermos == true,
            clientIp(http),
            http.getHeader(HttpHeaders.USER_AGENT),
        )
    }

    @GetMapping("/me")
    @Operation(summary = "Sessão atual (sem role; só authorities)")
    fun me(authentication: Authentication): SessaoResponse {
        val principal = authentication.principal as IamPrincipal
        return SessaoResponse.from(consultarSessaoUseCase.execute(principal.userId))
    }

    private fun withRefreshCookie(result: LoginResult): ResponseEntity<LoginResponse> =
        ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.create(result.refreshToken).toString())
            .body(LoginResponse.from(result))

    companion object {
        private fun clientIp(request: HttpServletRequest): String {
            val forwarded = request.getHeader("X-Forwarded-For")
            if (!forwarded.isNullOrBlank()) {
                return forwarded.split(",")[0].trim()
            }
            return request.remoteAddr
        }
    }
}
