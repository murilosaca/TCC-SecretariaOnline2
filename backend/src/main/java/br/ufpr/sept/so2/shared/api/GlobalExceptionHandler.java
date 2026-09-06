package br.ufpr.sept.so2.shared.api;

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERROR_BASE = "https://secretariaonline.ufpr.br/errors/";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return Map.of(
                                "campo", fieldError.getField(),
                                "mensagem", fieldError.getDefaultMessage() == null ? "inválido" : fieldError.getDefaultMessage()
                        );
                    }
                    return Map.of("mensagem", error.getDefaultMessage() == null ? "inválido" : error.getDefaultMessage());
                })
                .toList();
        ProblemDetail detail = problemDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Dados inválidos",
                "A requisição contém dados inválidos. Verifique os campos.",
                "validation-error"
        );
        detail.setProperty("erros", errors);
        return detail;
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail handleNotFound(RecursoNaoEncontradoException ex) {
        return problemDetail(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), "not-found");
    }

    @ExceptionHandler(DadoInvalidoException.class)
    public ProblemDetail handleInvalid(DadoInvalidoException ex) {
        return problemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Dados inválidos", ex.getMessage(), "validation-error");
    }

    @ExceptionHandler(ConflitoEstadoException.class)
    public ProblemDetail handleConflict(ConflitoEstadoException ex) {
        return problemDetail(HttpStatus.CONFLICT, "Conflito de estado", ex.getMessage(), "conflict");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problemDetail(HttpStatus.BAD_REQUEST, "Dados inválidos", ex.getMessage(), "bad-request");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        LOG.warn("Acesso negado: {} - {}", request.getDescription(false), ex.getMessage());
        return problemDetail(HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para esta operação.", "access-denied");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problemDetail(HttpStatus.UNAUTHORIZED, "Não autenticado", "Token inválido ou expirado.", "authentication-required");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, WebRequest request) {
        String incidentId = "INC-" + OffsetDateTime.now().getYear() + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        LOG.error("Erro inesperado [{}] em {}: {}", incidentId, request.getDescription(false), ex.getMessage(), ex);
        ProblemDetail detail = problemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente ou contate o suporte.",
                "internal-error"
        );
        detail.setProperty("incidentId", incidentId);
        return detail;
    }

    private ProblemDetail problemDetail(HttpStatus status, String title, String detail, String type) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(ERROR_BASE + type));
        problem.setProperty("timestamp", OffsetDateTime.now().toString());
        return problem;
    }
}
