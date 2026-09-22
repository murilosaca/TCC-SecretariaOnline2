package br.ufpr.sept.so2.modules.tcc.api

import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.modules.tcc.api.dto.RegistrarAvaliacaoTccRequest
import br.ufpr.sept.so2.modules.tcc.api.dto.TccResponse
import br.ufpr.sept.so2.modules.tcc.application.BaixarVersaoFinalTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.EnviarVersaoFinalTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.ListarFilaRevisaoTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.ListarMeusTccsUseCase
import br.ufpr.sept.so2.modules.tcc.application.ObterTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.RegistrarAvaliacaoTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.TccAcesso
import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort
import br.ufpr.sept.so2.shared.api.PageResponse
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/tccs")
@Tag(name = "TCCs", description = "TCC do aluno e avaliação individual do orientador ou da banca (RF-F1-008 / RF-F3-006)")
class TccController(
    private val listarMeusTccsUseCase: ListarMeusTccsUseCase,
    private val listarFilaRevisaoTccUseCase: ListarFilaRevisaoTccUseCase,
    private val obterTccUseCase: ObterTccUseCase,
    private val enviarVersaoFinalTccUseCase: EnviarVersaoFinalTccUseCase,
    private val registrarAvaliacaoTccUseCase: RegistrarAvaliacaoTccUseCase,
    private val baixarVersaoFinalTccUseCase: BaixarVersaoFinalTccUseCase,
    private val alunoTccPort: AlunoTccPort,
    private val assembler: TccAssembler,
) {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('tcc.view_own','tcc.review')")
    @Operation(summary = "Listar TCCs do aluno ou a fila individual do orientador/banca")
    fun listar(
        @RequestParam(name = "canReview", defaultValue = "false") canReview: Boolean,
        @RequestParam(defaultValue = "me") aluno: String,
        @RequestParam(required = false) estado: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<TccResponse> {
        val principal = principal(authentication)
        if (canReview) {
            if (TccAcesso.REVIEW !in principal.authorities) {
                throw AcessoNegadoException("Você não tem permissão para esta operação.")
            }
            val alunoId = alunoIdOuNulo(principal.userId)
            return PageResponse.ofWithLinks(
                listarFilaRevisaoTccUseCase.execute(principal.userId, estado, pageable),
                Function { item -> assembler.from(item, principal.userId, alunoId, principal.authorities) },
            )
        }
        if (TccAcesso.VIEW !in principal.authorities) {
            throw AcessoNegadoException("Você não tem permissão para esta operação.")
        }
        val alunoId = alunoIdOuNulo(principal.userId)
        return PageResponse.ofWithLinks(
            listarMeusTccsUseCase.execute(principal.userId, aluno, estado, pageable),
            Function { item -> assembler.from(item, principal.userId, alunoId, principal.authorities) },
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('tcc.view_own','tcc.review')")
    @Operation(summary = "Detalhe do TCC com banca, datas e avaliações")
    fun buscar(@PathVariable id: UUID, authentication: Authentication): TccResponse {
        val principal = principal(authentication)
        return responder(obterTccUseCase.execute(id, principal.userId, principal.authorities), principal)
    }

    @GetMapping("/{id}/arquivo")
    @PreAuthorize("hasAnyAuthority('tcc.view_own','tcc.review')")
    @Operation(summary = "Baixar o PDF da versão final armazenado pelo sistema")
    fun arquivo(@PathVariable id: UUID, authentication: Authentication): ResponseEntity<ByteArray> {
        val principal = principal(authentication)
        val arquivo = baixarVersaoFinalTccUseCase.execute(id, principal.userId, principal.authorities)
        val nome = arquivo.nome.replace(Regex("[^A-Za-z0-9._-]"), "_").ifEmpty { "tcc.pdf" }
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nome).build().toString())
            .body(arquivo.bytes)
    }

    @PostMapping("/{id}/versao-final", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAuthority('tcc.view_own')")
    @Operation(summary = "Enviar o PDF da versão final")
    fun enviarVersaoFinal(
        @PathVariable id: UUID,
        @RequestParam arquivo: MultipartFile,
        authentication: Authentication,
        http: HttpServletRequest,
    ): TccResponse {
        if (arquivo.isEmpty) {
            throw DadoInvalidoException("Envie um PDF de até 5 MB.")
        }
        val principal = principal(authentication)
        return responder(
            enviarVersaoFinalTccUseCase.execute(
                id,
                principal.userId,
                arquivo.originalFilename,
                arquivo.contentType,
                arquivo.bytes,
                clientIp(http),
            ),
            principal,
        )
    }

    @PostMapping("/{id}/avaliacoes")
    @PreAuthorize("hasAuthority('tcc.review')")
    @Operation(summary = "Registrar nota e parecer individual")
    fun avaliar(
        @PathVariable id: UUID,
        @RequestBody request: RegistrarAvaliacaoTccRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): TccResponse {
        val principal = principal(authentication)
        return responder(
            registrarAvaliacaoTccUseCase.execute(
                id,
                principal.userId,
                request.acao,
                request.nota,
                request.parecer,
                clientIp(http),
            ),
            principal,
        )
    }

    private fun responder(tcc: br.ufpr.sept.so2.modules.tcc.domain.Tcc, principal: IamPrincipal): TccResponse =
        assembler.from(tcc, principal.userId, alunoIdOuNulo(principal.userId), principal.authorities)

    private fun alunoIdOuNulo(usuarioId: UUID): UUID? = alunoTccPort.resolver(usuarioId)?.id

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
