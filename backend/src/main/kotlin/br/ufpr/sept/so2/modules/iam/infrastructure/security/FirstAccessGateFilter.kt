package br.ufpr.sept.so2.modules.iam.infrastructure.security

import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.api.ProblemResponses
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class FirstAccessGateFilter(
    private val usuarioRepository: UsuarioRepository,
    private val problemResponses: ProblemResponses,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = authentication?.principal as? IamPrincipal
        if (authentication == null || !authentication.isAuthenticated || principal == null) {
            filterChain.doFilter(request, response)
            return
        }
        if (!principal.mustChangePassword || senhaJaAlterada(principal)) {
            filterChain.doFilter(request, response)
            return
        }
        if (liberadoNoPrimeiroAcesso(request)) {
            filterChain.doFilter(request, response)
            return
        }
        problemResponses.write(
            request,
            response,
            HttpStatus.FORBIDDEN,
            "Primeiro acesso pendente",
            "Defina uma senha forte e aceite a LGPD antes de continuar.",
            "access-denied",
        )
    }

    private fun senhaJaAlterada(principal: IamPrincipal): Boolean =
        usuarioRepository.findById(principal.userId)
            .map { it.senhaAlterada }
            .orElse(false)

    companion object {
        private fun liberadoNoPrimeiroAcesso(request: HttpServletRequest): Boolean {
            val path = request.requestURI
            val method = request.method
            return (method == "POST" && path == "/auth/primeiro-acesso") ||
                (method == "POST" && path == "/auth/logout") ||
                (method == "POST" && path == "/auth/refresh") ||
                (method == "GET" && path == "/auth/me")
        }
    }
}
