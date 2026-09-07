package br.ufpr.sept.so2.shared.api

import br.ufpr.sept.so2.modules.iam.domain.SenhaReutilizadaException
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.CredenciaisInvalidasException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.RateLimitExcedidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.domain.exception.TokenResetInvalidoException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException): ProblemDetail =
        problemDetail(
            HttpStatus.BAD_REQUEST,
            "Dados inválidos",
            "Corpo da requisição ausente ou inválido.",
            "bad-request",
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ProblemDetail {
        val errors = ex.bindingResult.allErrors.map { error ->
            if (error is FieldError) {
                mapOf(
                    "campo" to error.field,
                    "mensagem" to (error.defaultMessage ?: "inválido"),
                )
            } else {
                mapOf("mensagem" to (error.defaultMessage ?: "inválido"))
            }
        }
        val detail = problemDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Dados inválidos",
            "A requisição contém dados inválidos. Verifique os campos.",
            "validation-error",
        )
        detail.setProperty("erros", errors)
        return detail
    }

    @ExceptionHandler(RecursoNaoEncontradoException::class)
    fun handleNotFound(ex: RecursoNaoEncontradoException): ProblemDetail =
        problemDetail(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.message, "not-found")

    @ExceptionHandler(SenhaReutilizadaException::class)
    fun handleSenhaReutilizada(ex: SenhaReutilizadaException): ProblemDetail =
        problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Senha reutilizada", ex.message, "senha-reutilizada")

    @ExceptionHandler(DadoInvalidoException::class)
    fun handleInvalid(ex: DadoInvalidoException): ProblemDetail =
        problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Dados inválidos", ex.message, "validation-error")

    @ExceptionHandler(CredenciaisInvalidasException::class)
    fun handleCredenciais(ex: CredenciaisInvalidasException): ProblemDetail =
        problemDetail(
            HttpStatus.UNAUTHORIZED,
            "Não autenticado",
            CredenciaisInvalidasException.MENSAGEM,
            "authentication-required",
        )

    @ExceptionHandler(TokenResetInvalidoException::class)
    fun handleResetToken(ex: TokenResetInvalidoException): ProblemDetail =
        problemDetail(
            HttpStatus.UNAUTHORIZED,
            "Não autenticado",
            TokenResetInvalidoException.MENSAGEM,
            "authentication-required",
        )

    @ExceptionHandler(RateLimitExcedidoException::class)
    fun handleRateLimit(ex: RateLimitExcedidoException): ProblemDetail {
        val detail = problemDetail(
            HttpStatus.TOO_MANY_REQUESTS,
            "Muitas tentativas",
            ex.message,
            "rate-limit",
        )
        detail.setProperty("retryAfterSeconds", ex.retryAfterSeconds)
        return detail
    }

    @ExceptionHandler(AcessoNegadoException::class)
    fun handleAcessoNegado(ex: AcessoNegadoException): ProblemDetail =
        problemDetail(HttpStatus.FORBIDDEN, "Acesso negado", ex.message, "access-denied")

    @ExceptionHandler(ConflitoEstadoException::class)
    fun handleConflict(ex: ConflitoEstadoException): ProblemDetail =
        problemDetail(HttpStatus.CONFLICT, "Conflito de estado", ex.message, "conflict")

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ProblemDetail =
        problemDetail(HttpStatus.BAD_REQUEST, "Dados inválidos", ex.message, "bad-request")

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException, request: WebRequest): ProblemDetail {
        LOG.warn("Acesso negado: {} - {}", request.getDescription(false), ex.message)
        return problemDetail(
            HttpStatus.FORBIDDEN,
            "Acesso negado",
            "Você não tem permissão para esta operação.",
            "access-denied",
        )
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthentication(ex: AuthenticationException): ProblemDetail =
        problemDetail(
            HttpStatus.UNAUTHORIZED,
            "Não autenticado",
            "Token inválido ou expirado.",
            "authentication-required",
        )

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: WebRequest): ProblemDetail {
        val incidentId = "INC-" + OffsetDateTime.now().year + "-" +
            UUID.randomUUID().toString().replace("-", "").substring(0, 4)
        LOG.error("Erro inesperado [{}] em {}: {}", incidentId, request.getDescription(false), ex.message, ex)
        val detail = problemDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro interno",
            "Ocorreu um erro inesperado. Tente novamente ou contate o suporte.",
            "internal-error",
        )
        detail.setProperty("incidentId", incidentId)
        return detail
    }

    private fun problemDetail(status: HttpStatus, title: String, detail: String?, type: String): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(status, detail)
        problem.title = title
        problem.type = URI.create(ERROR_BASE + type)
        problem.setProperty("timestamp", OffsetDateTime.now().toString())
        return problem
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
        private const val ERROR_BASE = "https://secretariaonline.ufpr.br/errors/"
    }
}
