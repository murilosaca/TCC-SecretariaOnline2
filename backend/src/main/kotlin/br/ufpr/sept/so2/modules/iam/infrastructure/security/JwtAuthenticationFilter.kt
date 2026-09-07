package br.ufpr.sept.so2.modules.iam.infrastructure.security

import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.shared.api.ProblemResponses
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenService: JwtTokenService,
    private val problemResponses: ProblemResponses,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }
        try {
            val claims = jwtTokenService.parseAccessToken(header.substring(7))
            val principal = IamPrincipal(
                claims.userId,
                claims.mustChangePassword,
                claims.authorities,
            )
            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                claims.authorities.map { SimpleGrantedAuthority(it) },
            )
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        } catch (_: RuntimeException) {
            SecurityContextHolder.clearContext()
            problemResponses.write(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                "Não autenticado",
                "Token inválido ou expirado.",
                "authentication-required",
            )
        }
    }
}
