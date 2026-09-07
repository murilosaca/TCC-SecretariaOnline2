package br.ufpr.sept.so2.modules.iam.infrastructure.security

import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import br.ufpr.sept.so2.shared.api.ProblemResponses
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.ArrayDeque
import java.util.Deque
import java.util.concurrent.ConcurrentHashMap

class AuthRateLimitFilter(
    private val properties: IamProperties,
    private val problemResponses: ProblemResponses,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    private val janelas = ConcurrentHashMap<String, Deque<Long>>()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (request.method != "POST") {
            return true
        }
        val path = request.requestURI
        return path != "/auth/login" && path != "/auth/recuperar-senha"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val wrapper = if (request is RepeatableBodyRequest) request else RepeatableBodyRequest(request)
        val body = wrapper.bodyAsString()
        val path = wrapper.requestURI
        val ip = clientIp(wrapper)
        val limite: Int
        val janelaMs: Long
        val chave: String
        if (path == "/auth/login") {
            limite = properties.loginPorMinuto
            janelaMs = 60_000L
            chave = "login:$ip:${campo(body, "identificador")}"
        } else {
            limite = properties.recuperarPorHora
            janelaMs = 3_600_000L
            chave = "recuperar:$ip:${campo(body, "email")}"
        }
        val retryAfter = registrar(chave, limite, janelaMs)
        if (retryAfter > 0) {
            problemResponses.write(
                wrapper,
                response,
                HttpStatus.TOO_MANY_REQUESTS,
                "Muitas tentativas",
                "Muitas tentativas. Aguarde antes de tentar novamente.",
                "rate-limit",
                mapOf("retryAfterSeconds" to retryAfter),
            )
            return
        }
        filterChain.doFilter(wrapper, response)
    }

    private fun registrar(chave: String, limite: Int, janelaMs: Long): Int {
        val agora = Instant.now().toEpochMilli()
        val inicio = agora - janelaMs
        val hits = janelas.computeIfAbsent(chave) { ArrayDeque() }
        synchronized(hits) {
            while (hits.isNotEmpty() && hits.peekFirst() < inicio) {
                hits.removeFirst()
            }
            if (hits.size >= limite) {
                val maisAntigo = hits.peekFirst()
                return maxOf(1, Math.ceil((maisAntigo + janelaMs - agora) / 1000.0).toInt())
            }
            hits.addLast(agora)
            return 0
        }
    }

    private fun campo(body: String?, nome: String): String {
        if (body.isNullOrBlank()) {
            return "vazio"
        }
        return try {
            val node = objectMapper.readTree(body)
            val valor = node.get(nome)
            if (valor == null || valor.asText().isBlank()) "vazio" else valor.asText().trim().lowercase()
        } catch (_: Exception) {
            "vazio"
        }
    }

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
