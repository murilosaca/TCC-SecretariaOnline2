package br.ufpr.sept.so2.modules.iam.infrastructure.security;

import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties;
import br.ufpr.sept.so2.shared.api.ProblemResponses;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final IamProperties properties;
    private final ProblemResponses problemResponses;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Deque<Long>> janelas = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(
            IamProperties properties,
            ProblemResponses problemResponses,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.problemResponses = problemResponses;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equals(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return !"/auth/login".equals(path) && !"/auth/recuperar-senha".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        RepeatableBodyRequest wrapper = request instanceof RepeatableBodyRequest cached
                ? cached
                : new RepeatableBodyRequest(request);
        String body = wrapper.bodyAsString();
        String path = wrapper.getRequestURI();
        String ip = clientIp(wrapper);
        int limite;
        long janelaMs;
        String chave;
        if ("/auth/login".equals(path)) {
            limite = properties.getLoginPorMinuto();
            janelaMs = 60_000L;
            chave = "login:" + ip + ":" + campo(body, "identificador");
        } else {
            limite = properties.getRecuperarPorHora();
            janelaMs = 3_600_000L;
            chave = "recuperar:" + ip + ":" + campo(body, "email");
        }
        int retryAfter = registrar(chave, limite, janelaMs);
        if (retryAfter > 0) {
            problemResponses.write(
                    wrapper,
                    response,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas",
                    "Muitas tentativas. Aguarde antes de tentar novamente.",
                    "rate-limit",
                    Map.of("retryAfterSeconds", retryAfter)
            );
            return;
        }
        filterChain.doFilter(wrapper, response);
    }

    private int registrar(String chave, int limite, long janelaMs) {
        long agora = Instant.now().toEpochMilli();
        long inicio = agora - janelaMs;
        Deque<Long> hits = janelas.computeIfAbsent(chave, ignored -> new ArrayDeque<>());
        synchronized (hits) {
            while (!hits.isEmpty() && hits.peekFirst() < inicio) {
                hits.removeFirst();
            }
            if (hits.size() >= limite) {
                long maisAntigo = hits.peekFirst();
                return (int) Math.max(1, Math.ceil((maisAntigo + janelaMs - agora) / 1000.0));
            }
            hits.addLast(agora);
            return 0;
        }
    }

    private String campo(String body, String nome) {
        if (body == null || body.isBlank()) {
            return "vazio";
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            JsonNode valor = node.get(nome);
            return valor == null || valor.asText().isBlank() ? "vazio" : valor.asText().trim().toLowerCase();
        } catch (Exception ex) {
            return "vazio";
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
