package br.ufpr.sept.so2.modules.iam.infrastructure.security;

import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService;
import br.ufpr.sept.so2.shared.api.ProblemResponses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final ProblemResponses problemResponses;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, ProblemResponses problemResponses) {
        this.jwtTokenService = jwtTokenService;
        this.problemResponses = problemResponses;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            JwtTokenService.AccessTokenClaims claims = jwtTokenService.parseAccessToken(header.substring(7));
            IamPrincipal principal = new IamPrincipal(
                    claims.userId(),
                    claims.mustChangePassword(),
                    claims.authorities()
            );
            var authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    claims.authorities().stream().map(SimpleGrantedAuthority::new).toList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
            problemResponses.write(
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Não autenticado",
                    "Token inválido ou expirado.",
                    "authentication-required"
            );
        }
    }
}
