package br.ufpr.sept.so2.modules.diplomas.api

import br.ufpr.sept.so2.modules.diplomas.api.dto.ConfirmarColacaoRequest
import br.ufpr.sept.so2.modules.diplomas.api.dto.ConfirmarEntregaRequest
import br.ufpr.sept.so2.modules.diplomas.api.dto.DiplomaResponse
import br.ufpr.sept.so2.modules.diplomas.api.dto.ElegiveisColacaoResponse
import br.ufpr.sept.so2.modules.diplomas.application.ConfirmarColacaoComando
import br.ufpr.sept.so2.modules.diplomas.application.ConfirmarColacaoUseCase
import br.ufpr.sept.so2.modules.diplomas.application.ConfirmarEntregaDiplomaUseCase
import br.ufpr.sept.so2.modules.diplomas.application.EnviarPdfDiplomaUseCase
import br.ufpr.sept.so2.modules.diplomas.application.ListarDiplomasUseCase
import br.ufpr.sept.so2.modules.diplomas.application.ListarElegiveisColacaoUseCase
import br.ufpr.sept.so2.modules.diplomas.application.ObterDiplomaUseCase
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/diplomas")
@Tag(name = "Diplomas", description = "Colação de grau e entrega de diploma (RF-F5-005-b / F5.11)")
class DiplomaController(
    private val listarElegiveisColacaoUseCase: ListarElegiveisColacaoUseCase,
    private val confirmarColacaoUseCase: ConfirmarColacaoUseCase,
    private val listarDiplomasUseCase: ListarDiplomasUseCase,
    private val obterDiplomaUseCase: ObterDiplomaUseCase,
    private val confirmarEntregaDiplomaUseCase: ConfirmarEntregaDiplomaUseCase,
    private val enviarPdfDiplomaUseCase: EnviarPdfDiplomaUseCase,
    private val assembler: DiplomaAssembler,
) {

    @GetMapping("/elegiveis")
    @PreAuthorize("hasAuthority('diploma.register')")
    @Operation(summary = "Lista alunos do curso/período com elegibilidade para colação")
    fun elegiveis(
        @RequestParam cursoId: UUID,
        @RequestParam periodoId: UUID,
        authentication: Authentication,
    ): ElegiveisColacaoResponse {
        val lista = listarElegiveisColacaoUseCase.execute(cursoId, periodoId)
        return assembler.elegiveis(lista, podeRegistrar(authentication))
    }

    @PostMapping
    @PreAuthorize("hasAuthority('diploma.register')")
    @Operation(summary = "Confirma colação em lote (TX atômica ALUNO → EGRESSO)")
    fun confirmar(
        @RequestBody body: ConfirmarColacaoRequest,
        authentication: Authentication,
        request: HttpServletRequest,
    ): List<DiplomaResponse> {
        val principal = principal(authentication)
        val diplomas = confirmarColacaoUseCase.execute(
            ConfirmarColacaoComando(
                body.cursoId,
                body.periodoId,
                body.alunoIds,
                body.dataColacao,
                body.livro,
                body.folha,
                body.turma,
            ),
            principal.userId,
            request.remoteAddr,
        )
        return diplomas.map { assembler.from(it, true) }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('diploma.register')")
    @Operation(summary = "Lista diplomas do curso (ex.: PENDENTE para entrega física)")
    fun listar(
        @RequestParam cursoId: UUID,
        @RequestParam(required = false) situacao: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<DiplomaResponse> {
        val pode = podeRegistrar(authentication)
        return PageResponse.ofWithLinks(
            listarDiplomasUseCase.execute(cursoId, situacao, pageable),
            Function { assembler.from(it, pode) },
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('diploma.register')")
    @Operation(summary = "Detalhe do diploma")
    fun obter(@PathVariable id: UUID, authentication: Authentication): DiplomaResponse =
        assembler.from(obterDiplomaUseCase.execute(id), podeRegistrar(authentication))

    @PatchMapping("/{id}/confirm-delivery")
    @PreAuthorize("hasAuthority('diploma.register')")
    @Operation(summary = "Confirma entrega física do diploma (PENDENTE → ENTREGUE)")
    fun confirmarEntrega(
        @PathVariable id: UUID,
        @RequestBody body: ConfirmarEntregaRequest,
        authentication: Authentication,
        request: HttpServletRequest,
    ): DiplomaResponse {
        val principal = principal(authentication)
        val diploma = confirmarEntregaDiplomaUseCase.execute(
            id,
            body.metodo,
            body.dataEntrega,
            principal.userId,
            request.remoteAddr,
        )
        return assembler.from(diploma, true)
    }

    @PostMapping("/{id}/pdf", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasAuthority('diploma.register')")
    @Operation(summary = "Upload do PDF oficial do diploma no MinIO")
    fun uploadPdf(
        @PathVariable id: UUID,
        @RequestPart("file") file: MultipartFile,
        authentication: Authentication,
        request: HttpServletRequest,
    ): DiplomaResponse {
        if (file.isEmpty) {
            throw DadoInvalidoException("PDF do diploma é obrigatório.")
        }
        val principal = principal(authentication)
        val diploma = enviarPdfDiplomaUseCase.execute(
            id,
            file.originalFilename,
            file.contentType,
            file.bytes,
            principal.userId,
            request.remoteAddr,
        )
        return assembler.from(diploma, true)
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal

        private fun podeRegistrar(authentication: Authentication): Boolean =
            authentication.authorities.any { it.authority == "diploma.register" }
    }
}
