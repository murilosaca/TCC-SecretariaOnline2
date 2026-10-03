package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.api.dto.SessaoDispositivoResponse
import br.ufpr.sept.so2.modules.iam.api.dto.SessoesResponse
import br.ufpr.sept.so2.modules.iam.application.EncerrarSessaoUseCase
import br.ufpr.sept.so2.modules.iam.application.ListarSessoesUseCase
import br.ufpr.sept.so2.modules.iam.application.SessaoAtualResolver
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/me/sessions")
@PreAuthorize("hasAuthority('user.update_own_profile')")
@Tag(name = "Sessões", description = "Sessões ativas do usuário autenticado (F1.4)")
class SessaoDispositivoController(
    private val listarSessoesUseCase: ListarSessoesUseCase,
    private val encerrarSessaoUseCase: EncerrarSessaoUseCase,
    private val sessaoAtualResolver: SessaoAtualResolver,
) {
    @GetMapping
    @Operation(summary = "Listar sessões ativas; a atual não recebe _links.encerrar")
    fun listar(
        authentication: Authentication,
        @CookieValue(name = "\${app.iam.cookie-name:so2_refresh}", required = false) refreshCookie: String?,
    ): SessoesResponse {
        val atual = principal(authentication)
        val sessaoId = sessaoAtualResolver.resolver(atual.sessionId, refreshCookie, atual.userId)
        val itens = listarSessoesUseCase.execute(atual.userId, sessaoId).map { SessaoDispositivoResponse.from(it) }
        return SessoesResponse(itens)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerrar outra sessão (a atual é recusada)")
    fun encerrar(
        @PathVariable id: UUID,
        authentication: Authentication,
        @CookieValue(name = "\${app.iam.cookie-name:so2_refresh}", required = false) refreshCookie: String?,
        http: HttpServletRequest,
    ) {
        val atual = principal(authentication)
        encerrarSessaoUseCase.execute(
            atual.userId,
            id,
            sessaoAtualResolver.resolver(atual.sessionId, refreshCookie, atual.userId),
            ClienteIp.de(http),
        )
    }

    private fun principal(authentication: Authentication): IamPrincipal =
        authentication.principal as IamPrincipal
}
