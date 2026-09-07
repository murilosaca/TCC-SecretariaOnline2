package br.ufpr.sept.so2.shared.api

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Component
import java.net.URI
import java.time.OffsetDateTime

@Component
class ProblemResponses(
    private val objectMapper: ObjectMapper,
) {
    fun create(status: HttpStatus, title: String, detail: String, type: String): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(status, detail)
        problem.title = title
        problem.type = URI.create(ERROR_BASE + type)
        problem.setProperty("timestamp", OffsetDateTime.now().toString())
        return problem
    }

    @JvmOverloads
    fun write(
        request: HttpServletRequest,
        response: HttpServletResponse,
        status: HttpStatus,
        title: String,
        detail: String,
        type: String,
        extras: Map<String, Any> = emptyMap(),
    ) {
        if (response.isCommitted) {
            return
        }
        val body = linkedMapOf<String, Any>(
            "type" to (ERROR_BASE + type),
            "title" to title,
            "status" to status.value(),
            "detail" to detail,
            "instance" to request.requestURI,
            "timestamp" to OffsetDateTime.now().toString(),
        )
        extras.forEach { (key, value) -> body[key] = value }
        response.status = status.value()
        response.contentType = "application/problem+json"
        objectMapper.writeValue(response.writer, body)
    }

    companion object {
        const val ERROR_BASE: String = "https://secretariaonline.ufpr.br/errors/"
    }
}
