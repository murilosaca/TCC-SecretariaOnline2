package br.ufpr.sept.so2.modules.tcc.api

import br.ufpr.sept.so2.modules.arquivos.api.dto.DownloadUrlResponse
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.modules.tcc.api.dto.RegistrarAvaliacaoTccRequest
import br.ufpr.sept.so2.modules.tcc.api.dto.RegistrarTccRequest
import br.ufpr.sept.so2.modules.tcc.api.dto.TccResponse
import br.ufpr.sept.so2.modules.tcc.application.AtualizarTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.BaixarVersaoFinalTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.EnviarVersaoFinalTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.ListarFilaRevisaoTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.ListarMeusTccsUseCase
import br.ufpr.sept.so2.modules.tcc.application.ListarTccsDoEscopoUseCase
import br.ufpr.sept.so2.modules.tcc.application.ObterTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.RegistrarAvaliacaoTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.RegistrarTccUseCase
import br.ufpr.sept.so2.modules.tcc.application.TccAcesso
import br.ufpr.sept.so2.modules.tcc.application.ports.AlunoTccPort
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
@RequestMapping("/tccs")
@Tag(name = "TCCs", description = "TCC do aluno e avaliação individual do orientador ou da banca (RF-F1-008 / RF-F3-006)")
class TccController(
    private val listarMeusTccsUseCase: ListarMeusTccsUseCase,
    private val listarFilaRevisaoTccUseCase: ListarFilaRevisaoTccUseCase,
    private val listarTccsDoEscopoUseCase: ListarTccsDoEscopoUseCase,
    private val registrarTccUseCase: RegistrarTccUseCase,
    private val atualizarTccUseCase: AtualizarTccUseCase,
    private val obterTccUseCase: ObterTccUseCase,
    private val enviarVersaoFinalTccUseCase: EnviarVersaoFinalTccUseCase,
    private val registrarAvaliacaoTccUseCase: RegistrarAvaliacaoTccUseCase,
    private val baixarVersaoFinalTccUseCase: BaixarVersaoFinalTccUseCase,
    private val alunoTccPort: AlunoTccPort,
    private val assembler: TccAssembler,
) {

    @GetMapping(params = ["!escopo"])
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

    @GetMapping(params = ["escopo=cursos"])
    @PreAuthorize("hasAuthority('tcc.manage')")
    @Operation(summary = "Listar TCCs dos cursos da secretaria")
    fun listarDoEscopo(
        @RequestParam(required = false) situacao: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<TccResponse> {
        val principal = principal(authentication)
        val alunoId = alunoIdOuNulo(principal.userId)
        return PageResponse.ofWithLinks(
            listarTccsDoEscopoUseCase.execute(principal.userId, situacao, pageable),
            Function { item -> assembler.from(item, principal.userId, alunoId, principal.authorities) },
            mapOf("novo" to "/tccs"),
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('tcc.manage')")
    @Operation(summary = "Registrar TCC ativo com banca")
    fun registrar(
        @Valid @RequestBody request: RegistrarTccRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): TccResponse {
        val principal = principal(authentication)
        return responder(
            registrarTccUseCase.execute(
                principal.userId,
                request.alunoId!!,
                request.titulo!!,
                request.dataDefesa!!,
                request.dataEntrega!!,
                membrosDe(request),
                clientIp(http),
            ),
            principal,
        )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('tcc.manage')")
    @Operation(summary = "Atualizar TCC ativo do escopo da secretaria")
    fun atualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RegistrarTccRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): TccResponse {
        val principal = principal(authentication)
        return responder(
            atualizarTccUseCase.execute(
                principal.userId,
                id,
                request.alunoId!!,
                request.titulo!!,
                request.dataDefesa!!,
                request.dataEntrega!!,
                membrosDe(request),
                clientIp(http),
            ),
            principal,
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
    @Operation(summary = "URL pré-assinada do PDF da versão final (TTL 15 min)")
    fun arquivo(@PathVariable id: UUID, authentication: Authentication): DownloadUrlResponse {
        val principal = principal(authentication)
        val arquivo = baixarVersaoFinalTccUseCase.execute(id, principal.userId, principal.authorities)
        return DownloadUrlResponse(arquivo.downloadUrl, arquivo.expiresInSeconds)
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

        private fun membrosDe(request: RegistrarTccRequest): List<Pair<UUID, String>> =
            request.membros!!.map { it.idUsuario!! to it.papel!! }
    }
}
