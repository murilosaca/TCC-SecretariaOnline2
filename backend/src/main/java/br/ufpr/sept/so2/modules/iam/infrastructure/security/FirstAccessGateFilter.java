package br.ufpr.sept.so2.modules.iam.infrastructure.security;

import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.api.ProblemResponses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class FirstAccessGateFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;
    private final ProblemResponses problemResponses;

    public FirstAccessGateFilter(UsuarioRepository usuarioRepository, ProblemResponses problemResponses) {
        this.usuarioRepository = usuarioRepository;
        this.problemResponses = problemResponses;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof IamPrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!principal.mustChangePassword() || senhaJaAlterada(principal)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (liberadoNoPrimeiroAcesso(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        problemResponses.write(
                request,
                response,
                HttpStatus.FORBIDDEN,
                "Primeiro acesso pendente",
                "Defina uma senha forte e aceite a LGPD antes de continuar.",
                "access-denied"
        );
    }

    private boolean senhaJaAlterada(IamPrincipal principal) {
        return usuarioRepository.findById(principal.userId())
                .map(Usuario::isSenhaAlterada)
                .orElse(false);
    }

    private static boolean liberadoNoPrimeiroAcesso(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        return ("POST".equals(method) && "/auth/primeiro-acesso".equals(path))
                || ("POST".equals(method) && "/auth/logout".equals(path))
                || ("POST".equals(method) && "/auth/refresh".equals(path))
                || ("GET".equals(method) && "/auth/me".equals(path));
    }
}
