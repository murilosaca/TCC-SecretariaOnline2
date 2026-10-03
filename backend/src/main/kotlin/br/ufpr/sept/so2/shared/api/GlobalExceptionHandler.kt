package br.ufpr.sept.so2.shared.api

import br.ufpr.sept.so2.modules.iam.domain.SenhaAtualIncorretaException
import br.ufpr.sept.so2.modules.iam.domain.SenhaReutilizadaException
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.CredenciaisInvalidasException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.LoteConflitoException
import br.ufpr.sept.so2.shared.domain.exception.PerfilEmUsoException
import br.ufpr.sept.so2.shared.domain.exception.RateLimitExcedidoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import br.ufpr.sept.so2.shared.domain.exception.SchemaInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.TipoSolicitacaoEmUsoException
import br.ufpr.sept.so2.shared.domain.exception.TokenAcaoInvalidoException
import br.ufpr.sept.so2.shared.domain.exception.TokenResetInvalidoException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.resource.NoResourceFoundException
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

    @ExceptionHandler(RecursoNaoEncontradoException::class, NoResourceFoundException::class)
    fun handleNotFound(ex: Exception): ProblemDetail =
        problemDetail(
            HttpStatus.NOT_FOUND,
            "Recurso não encontrado",
            if (ex is RecursoNaoEncontradoException) ex.message else "Recurso não encontrado.",
            "not-found",
        )

    @ExceptionHandler(SenhaReutilizadaException::class)
    fun handleSenhaReutilizada(ex: SenhaReutilizadaException): ProblemDetail =
        problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Senha reutilizada", ex.message, "senha-reutilizada")

    @ExceptionHandler(DadoInvalidoException::class)
    fun handleInvalid(ex: DadoInvalidoException): ProblemDetail =
        problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Dados inválidos", ex.message, "validation-error")

    @ExceptionHandler(PerfilEmUsoException::class)
    fun handlePerfilEmUso(ex: PerfilEmUsoException): ProblemDetail =
        problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Perfil em uso", ex.message, "role-in-use")

    @ExceptionHandler(TipoSolicitacaoEmUsoException::class)
    fun handleTipoEmUso(ex: TipoSolicitacaoEmUsoException): ProblemDetail =
        problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Tipo em uso", ex.message, "request-type-in-use")

    @ExceptionHandler(SchemaInvalidoException::class)
    fun handleSchema(ex: SchemaInvalidoException): ProblemDetail =
        problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Schema inválido", ex.message, ex.tipo)

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleUpload(ex: MaxUploadSizeExceededException): ProblemDetail =
        problemDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Dados inválidos",
            "Arquivo acima do tamanho aceito.",
            "validation-error",
        )

    @ExceptionHandler(SenhaAtualIncorretaException::class)
    fun handleSenhaAtual(ex: SenhaAtualIncorretaException): ProblemDetail =
        problemDetail(HttpStatus.UNAUTHORIZED, "Senha atual incorreta", ex.message, "senha-atual-incorreta")

    @ExceptionHandler(CredenciaisInvalidasException::class)
    fun handleCredenciais(ex: CredenciaisInvalidasException): ProblemDetail =
        problemDetail(
            HttpStatus.UNAUTHORIZED,
            "Não autenticado",
            CredenciaisInvalidasException.MENSAGEM,
            "authentication-required",
        )

    @ExceptionHandler(TokenResetInvalidoException::class, TokenAcaoInvalidoException::class)
    fun handleTokenUsoUnico(ex: RuntimeException): ProblemDetail =
        problemDetail(
            HttpStatus.UNAUTHORIZED,
            "Não autenticado",
            ex.message ?: "Link inválido ou expirado.",
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

    @ExceptionHandler(LoteConflitoException::class)
    fun handleLote(ex: LoteConflitoException): ProblemDetail {
        val detail = problemDetail(HttpStatus.CONFLICT, "Conflito de lote", ex.message, "bulk-conflict")
        detail.setProperty("failedIds", ex.failedIds.map { it.toString() })
        return detail
    }

    @ExceptionHandler(ConflitoEstadoException::class)
    fun handleConflict(ex: ConflitoEstadoException): ProblemDetail =
        problemDetail(HttpStatus.CONFLICT, "Conflito de estado", ex.message, "conflict")

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethod(ex: HttpRequestMethodNotSupportedException): ProblemDetail =
        problemDetail(
            HttpStatus.METHOD_NOT_ALLOWED,
            "Método não permitido",
            "Esta operação não existe.",
            "method-not-allowed",
        )

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException): ProblemDetail {
        LOG.warn("Violação de integridade: {}", resumoViolacaoIntegridade(ex))
        return problemDetail(
            HttpStatus.CONFLICT,
            "Conflito de estado",
            "O recurso conflita com um registro já existente.",
            "conflict",
        )
    }

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
        private val CONSTRAINT = Regex(
            """constraint\s+["'`]?([A-Za-z0-9_]+)["'`]?""",
            RegexOption.IGNORE_CASE,
        )

        fun resumoViolacaoIntegridade(ex: DataIntegrityViolationException): String {
            val cause = ex.mostSpecificCause
            val sqlConstraint = (cause as? java.sql.SQLException)?.let { sql ->
                sql.message?.let { CONSTRAINT.find(it)?.groupValues?.get(1) }
            }
            val constraint = sqlConstraint ?: CONSTRAINT.find(cause.message.orEmpty())?.groupValues?.get(1)
            return if (constraint != null) {
                "${cause.javaClass.simpleName} constraint=$constraint"
            } else {
                cause.javaClass.simpleName
            }
        }
    }
}
