package br.ufpr.sept.so2.modules.estagio.api

import br.ufpr.sept.so2.modules.estagio.api.dto.EmitirParecerEstagioRequest
import br.ufpr.sept.so2.modules.estagio.api.dto.EstagioResponse
import br.ufpr.sept.so2.modules.estagio.api.dto.RegistrarEstagioRequest
import br.ufpr.sept.so2.modules.estagio.application.AtualizarEstagioUseCase
import br.ufpr.sept.so2.modules.estagio.application.EncerrarEstagioUseCase
import br.ufpr.sept.so2.modules.estagio.application.EnviarDocumentoEstagioUseCase
import br.ufpr.sept.so2.modules.estagio.application.EstagioAcesso
import br.ufpr.sept.so2.modules.estagio.application.EmitirParecerEstagioUseCase
import br.ufpr.sept.so2.modules.estagio.application.ListarEstagiosDoEscopoUseCase
import br.ufpr.sept.so2.modules.estagio.application.ListarFilaRevisaoEstagioUseCase
import br.ufpr.sept.so2.modules.estagio.application.ListarMeusEstagiosUseCase
import br.ufpr.sept.so2.modules.estagio.application.ObterEstagioUseCase
import br.ufpr.sept.so2.modules.estagio.application.RegistrarEstagioUseCase
import br.ufpr.sept.so2.modules.estagio.application.ports.AlunoEstagioPort
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/estagios")
@Tag(name = "Estágios", description = "Estágios do aluno e parecer individual do orientador (RF-F1-007 / RF-F3-005)")
class EstagioController(
    private val listarMeusEstagiosUseCase: ListarMeusEstagiosUseCase,
    private val listarFilaRevisaoEstagioUseCase: ListarFilaRevisaoEstagioUseCase,
    private val listarEstagiosDoEscopoUseCase: ListarEstagiosDoEscopoUseCase,
    private val registrarEstagioUseCase: RegistrarEstagioUseCase,
    private val atualizarEstagioUseCase: AtualizarEstagioUseCase,
    private val obterEstagioUseCase: ObterEstagioUseCase,
    private val enviarDocumentoEstagioUseCase: EnviarDocumentoEstagioUseCase,
    private val emitirParecerEstagioUseCase: EmitirParecerEstagioUseCase,
    private val encerrarEstagioUseCase: EncerrarEstagioUseCase,
    private val alunoEstagioPort: AlunoEstagioPort,
    private val assembler: EstagioAssembler,
) {

    @GetMapping(params = ["!escopo"])
    @PreAuthorize("hasAnyAuthority('internship.view_own','internship.review')")
    @Operation(summary = "Listar estágios do aluno ou a fila individual do orientador")
    fun listar(
        @RequestParam(name = "canReview", defaultValue = "false") canReview: Boolean,
        @RequestParam(defaultValue = "me") aluno: String,
        @RequestParam(required = false) situacao: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<EstagioResponse> {
        val principal = principal(authentication)
        if (canReview) {
            if (EstagioAcesso.REVIEW !in principal.authorities) {
                throw AcessoNegadoException("Você não tem permissão para esta operação.")
            }
            val alunoId = alunoIdOuNulo(principal.userId)
            return PageResponse.ofWithLinks(
                listarFilaRevisaoEstagioUseCase.execute(principal.userId, situacao, pageable),
                Function { item -> assembler.from(item, principal.userId, alunoId, principal.authorities) },
            )
        }
        if (EstagioAcesso.VIEW !in principal.authorities) {
            throw AcessoNegadoException("Você não tem permissão para esta operação.")
        }
        val alunoId = alunoIdOuNulo(principal.userId)
        return PageResponse.ofWithLinks(
            listarMeusEstagiosUseCase.execute(principal.userId, aluno, situacao, pageable),
            Function { item -> assembler.from(item, principal.userId, alunoId, principal.authorities) },
        )
    }

    @GetMapping(params = ["escopo=cursos"])
    @PreAuthorize("hasAuthority('internship.manage')")
    @Operation(summary = "Listar estágios dos cursos da secretaria")
    fun listarDoEscopo(
        @RequestParam(required = false) situacao: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<EstagioResponse> {
        val principal = principal(authentication)
        val alunoId = alunoIdOuNulo(principal.userId)
        return PageResponse.ofWithLinks(
            listarEstagiosDoEscopoUseCase.execute(principal.userId, situacao, pageable),
            Function { item -> assembler.from(item, principal.userId, alunoId, principal.authorities) },
            mapOf("novo" to "/estagios"),
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('internship.manage')")
    @Operation(summary = "Registrar estágio ativo sem orientador")
    fun registrar(
        @Valid @RequestBody request: RegistrarEstagioRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): EstagioResponse {
        val principal = principal(authentication)
        return responder(
            registrarEstagioUseCase.execute(
                principal.userId,
                request.alunoId!!,
                request.empresa!!,
                request.supervisor!!,
                request.inicio!!,
                request.fim!!,
                clientIp(http),
            ),
            principal,
        )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('internship.manage')")
    @Operation(summary = "Atualizar estágio ativo do escopo da secretaria")
    fun atualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RegistrarEstagioRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): EstagioResponse {
        val principal = principal(authentication)
        return responder(
            atualizarEstagioUseCase.execute(
                principal.userId,
                id,
                request.alunoId!!,
                request.empresa!!,
                request.supervisor!!,
                request.inicio!!,
                request.fim!!,
                clientIp(http),
            ),
            principal,
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('internship.view_own','internship.review')")
    @Operation(summary = "Detalhe do estágio com documentos e pareceres")
    fun buscar(@PathVariable id: UUID, authentication: Authentication): EstagioResponse {
        val principal = principal(authentication)
        return responder(
            obterEstagioUseCase.execute(id, principal.userId, principal.authorities),
            principal,
        )
    }

    @PostMapping("/{id}/documentos", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAuthority('internship.view_own')")
    @Operation(summary = "Enviar PDF de um documento do estágio")
    fun enviarDocumento(
        @PathVariable id: UUID,
        @RequestParam tipo: String,
        @RequestParam arquivo: MultipartFile,
        authentication: Authentication,
        http: HttpServletRequest,
    ): EstagioResponse {
        if (arquivo.isEmpty) {
            throw DadoInvalidoException("Envie um PDF de até 5 MB.")
        }
        val principal = principal(authentication)
        return responder(
            enviarDocumentoEstagioUseCase.execute(
                id,
                principal.userId,
                tipo,
                arquivo.originalFilename,
                arquivo.contentType,
                arquivo.bytes,
                clientIp(http),
            ),
            principal,
        )
    }

    @PostMapping("/{id}/documentos/{documentoId}/parecer")
    @PreAuthorize("hasAuthority('internship.review')")
    @Operation(summary = "Emitir parecer individual em um documento")
    fun parecer(
        @PathVariable id: UUID,
        @PathVariable documentoId: UUID,
        @RequestBody request: EmitirParecerEstagioRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): EstagioResponse {
        val principal = principal(authentication)
        return responder(
            emitirParecerEstagioUseCase.execute(
                id,
                documentoId,
                principal.userId,
                request.acao,
                request.parecer,
                clientIp(http),
            ),
            principal,
        )
    }

    @PostMapping("/{id}/encerrar")
    @PreAuthorize("hasAuthority('internship.review')")
    @Operation(summary = "Arquivar estágio com todos os documentos obrigatórios aprovados")
    fun encerrar(
        @PathVariable id: UUID,
        authentication: Authentication,
        http: HttpServletRequest,
    ): EstagioResponse {
        val principal = principal(authentication)
        return responder(
            encerrarEstagioUseCase.execute(id, principal.userId, clientIp(http)),
            principal,
        )
    }

    private fun responder(
        estagio: br.ufpr.sept.so2.modules.estagio.domain.Estagio,
        principal: IamPrincipal,
    ): EstagioResponse = assembler.from(estagio, principal.userId, alunoIdOuNulo(principal.userId), principal.authorities)

    private fun alunoIdOuNulo(usuarioId: UUID): UUID? = alunoEstagioPort.resolver(usuarioId)?.id

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal

        private fun clientIp(request: HttpServletRequest): String {
            val forwarded = request.getHeader("X-Forwarded-For")
            if (!forwarded.isNullOrBlank()) {
                return forwarded.split(",")[0].trim()
            }
            return request.remoteAddr
        }
    }
}
