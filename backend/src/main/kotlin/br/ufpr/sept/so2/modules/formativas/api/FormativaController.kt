package br.ufpr.sept.so2.modules.formativas.api

import br.ufpr.sept.so2.modules.arquivos.api.dto.DownloadUrlResponse
import br.ufpr.sept.so2.modules.arquivos.application.PresignDownloadUseCase
import br.ufpr.sept.so2.modules.formativas.api.dto.FormativaResponse
import br.ufpr.sept.so2.modules.formativas.api.dto.RevisarFormativaRequest
import br.ufpr.sept.so2.modules.formativas.api.dto.TiposAtividadeResponse
import br.ufpr.sept.so2.modules.formativas.application.BaixarComprovanteFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.application.CancelarFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.application.ConfirmarFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.application.ListarFilaRevisaoFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.application.ListarMinhasFormativasUseCase
import br.ufpr.sept.so2.modules.formativas.application.ListarTiposAtividadeFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.application.ObterFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.application.RevisarFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.application.SubmeterFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
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
import java.net.URI
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/formativas")
@Tag(name = "Formativas", description = "Atividades formativas e revisão individual CAAF (RF-F1-006 / RF-F3-004)")
class FormativaController(
    private val listarMinhasFormativasUseCase: ListarMinhasFormativasUseCase,
    private val listarFilaRevisaoFormativaUseCase: ListarFilaRevisaoFormativaUseCase,
    private val obterFormativaUseCase: ObterFormativaUseCase,
    private val confirmarFormativaUseCase: ConfirmarFormativaUseCase,
    private val cancelarFormativaUseCase: CancelarFormativaUseCase,
    private val revisarFormativaUseCase: RevisarFormativaUseCase,
    private val listarTiposUseCase: ListarTiposAtividadeFormativaUseCase,
    private val submeterFormativaUseCase: SubmeterFormativaUseCase,
    private val baixarComprovanteUseCase: BaixarComprovanteFormativaUseCase,
    private val presignDownloadUseCase: PresignDownloadUseCase,
    private val alunoPorUsuarioPort: AlunoPorUsuarioPort,
    private val assembler: FormativaAssembler,
) {

    @GetMapping
    @PreAuthorize("hasAnyAuthority('formative.view_own','formative.review')")
    @Operation(summary = "Listar formativas do aluno ou a fila de revisão CAAF")
    fun listar(
        @RequestParam(name = "canReview", defaultValue = "false") canReview: Boolean,
        @RequestParam(defaultValue = "me") audience: String,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<FormativaResponse> {
        val principal = principal(authentication)
        if (canReview) {
            if (!principal.authorities.contains(AUTHORITY_REVIEW)) {
                throw AcessoNegadoException("Você não tem permissão para esta operação.")
            }
            return PageResponse.ofWithLinks(
                listarFilaRevisaoFormativaUseCase.execute(pageable),
                Function { item -> assembler.from(item, alunoIdOuNulo(principal.userId), principal.authorities) },
            )
        }
        if (!principal.authorities.contains(AUTHORITY_VIEW)) {
            throw AcessoNegadoException("Você não tem permissão para esta operação.")
        }
        val alunoId = alunoId(principal.userId)
        val extras = if (principal.authorities.contains(AUTHORITY_SUBMIT)) {
            mapOf("nova" to "/formativas/nova")
        } else {
            emptyMap()
        }
        return PageResponse.ofWithLinks(
            listarMinhasFormativasUseCase.execute(principal.userId, audience, pageable),
            Function { item -> assembler.from(item, alunoId, principal.authorities) },
            extras,
        )
    }

    @GetMapping("/tipos")
    @PreAuthorize("hasAuthority('formative.submit')")
    @Operation(summary = "Tipos de atividade formativa elegíveis ao curso do aluno")
    fun tipos(authentication: Authentication): TiposAtividadeResponse =
        TiposAtividadeResponse.from(listarTiposUseCase.execute(principal(authentication).userId))

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAuthority('formative.submit')")
    @Operation(summary = "Submeter formativa manual com comprovante")
    fun submeter(
        @RequestParam(required = false) tipoId: String?,
        @RequestParam(required = false) cargaHoraria: String?,
        @RequestParam(required = false) arquivo: MultipartFile?,
        authentication: Authentication,
    ): ResponseEntity<FormativaResponse> {
        val principal = principal(authentication)
        val salva = submeterFormativaUseCase.execute(
            principal.userId,
            uuid(tipoId),
            horas(cargaHoraria),
            arquivo?.takeIf { !it.isEmpty }?.bytes,
        )
        val corpo = assembler.from(salva, alunoId(principal.userId), principal.authorities)
        return ResponseEntity.created(URI.create("/formativas/${salva.id}")).body(corpo)
    }

    @GetMapping("/{id}/comprovante")
    @PreAuthorize("hasAnyAuthority('formative.view_own','formative.review')")
    @Operation(summary = "URL pré-assinada do comprovante")
    fun comprovante(@PathVariable id: UUID, authentication: Authentication): DownloadUrlResponse {
        val principal = principal(authentication)
        val url = baixarComprovanteUseCase.execute(id, principal.userId, principal.authorities)
        return DownloadUrlResponse(url, presignDownloadUseCase.ttlSeconds())
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('formative.view_own','formative.review')")
    @Operation(summary = "Detalhe da formativa")
    fun buscar(@PathVariable id: UUID, authentication: Authentication): FormativaResponse {
        val principal = principal(authentication)
        return assembler.from(
            obterFormativaUseCase.execute(id, principal.userId, principal.authorities),
            alunoIdOuNulo(principal.userId),
            principal.authorities,
        )
    }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAuthority('formative.confirm_own')")
    @Operation(summary = "Confirmar formativa pré-validada por presença")
    fun confirmar(@PathVariable id: UUID, authentication: Authentication): FormativaResponse {
        val principal = principal(authentication)
        return assembler.from(
            confirmarFormativaUseCase.execute(id, principal.userId),
            alunoId(principal.userId),
            principal.authorities,
        )
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('formative.view_own')")
    @Operation(summary = "Cancelar formativa pendente de confirmação")
    fun cancelar(@PathVariable id: UUID, authentication: Authentication): FormativaResponse {
        val principal = principal(authentication)
        return assembler.from(
            cancelarFormativaUseCase.execute(id, principal.userId),
            alunoId(principal.userId),
            principal.authorities,
        )
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasAuthority('formative.review')")
    @Operation(summary = "Aprovar formativa aguardando CAAF")
    fun aprovar(
        @PathVariable id: UUID,
        @RequestBody request: RevisarFormativaRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): FormativaResponse {
        val principal = principal(authentication)
        return assembler.from(
            revisarFormativaUseCase.execute(
                id,
                principal.userId,
                RevisarFormativaUseCase.ACAO_APROVAR,
                request.parecer,
                clientIp(http),
            ),
            alunoIdOuNulo(principal.userId),
            principal.authorities,
        )
    }

    @PostMapping("/{id}/indeferir")
    @PreAuthorize("hasAuthority('formative.review')")
    @Operation(summary = "Indeferir formativa aguardando CAAF")
    fun indeferir(
        @PathVariable id: UUID,
        @RequestBody request: RevisarFormativaRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): FormativaResponse {
        val principal = principal(authentication)
        return assembler.from(
            revisarFormativaUseCase.execute(
                id,
                principal.userId,
                RevisarFormativaUseCase.ACAO_INDEFERIR,
                request.parecer,
                clientIp(http),
            ),
            alunoIdOuNulo(principal.userId),
            principal.authorities,
        )
    }

    private fun alunoId(usuarioId: UUID): UUID =
        alunoPorUsuarioPort.resolver(usuarioId)?.id
            ?: throw AcessoNegadoException("Cadastro acadêmico de aluno não encontrado.")

    private fun alunoIdOuNulo(usuarioId: UUID): UUID? = alunoPorUsuarioPort.resolver(usuarioId)?.id

    companion object {
        private const val AUTHORITY_VIEW = "formative.view_own"
        private const val AUTHORITY_REVIEW = "formative.review"
        private const val AUTHORITY_SUBMIT = "formative.submit"

        private fun uuid(raw: String?): UUID? {
            if (raw.isNullOrBlank()) {
                return null
            }
            return try {
                UUID.fromString(raw.trim())
            } catch (_: IllegalArgumentException) {
                throw DadoInvalidoException(SubmeterFormativaUseCase.FORA_DO_CURSO)
            }
        }

        private fun horas(raw: String?): Int? {
            if (raw.isNullOrBlank()) {
                return null
            }
            return raw.trim().toIntOrNull()
                ?: throw DadoInvalidoException("Carga horária deve ser positiva.")
        }

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
