package br.ufpr.sept.so2.modules.importacao.api

import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.modules.importacao.api.dto.ImportacaoJobResponse
import br.ufpr.sept.so2.modules.importacao.application.ConfirmarImportacaoUseCase
import br.ufpr.sept.so2.modules.importacao.application.ObterImportacaoUseCase
import br.ufpr.sept.so2.modules.importacao.application.ReceberImportacaoUseCase
import br.ufpr.sept.so2.modules.importacao.domain.ImportacaoKinds
import br.ufpr.sept.so2.shared.api.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/importacoes")
@Tag(name = "Importações", description = "Wizard CSV/XLSX da secretaria (RF-F5-009)")
class ImportacaoController(
    private val receberImportacaoUseCase: ReceberImportacaoUseCase,
    private val obterImportacaoUseCase: ObterImportacaoUseCase,
    private val confirmarImportacaoUseCase: ConfirmarImportacaoUseCase,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('import.run')")
    @Operation(summary = "Histórico de importações do operador")
    fun listar(
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<ImportacaoJobResponse> =
        PageResponse.ofWithLinks(
            obterImportacaoUseCase.listar(principal(authentication).userId, pageable),
            Function { ImportacaoJobResponse.de(it, false) },
        )

    @GetMapping("/modelos/{kind}")
    @PreAuthorize("hasAuthority('import.run')")
    @Operation(summary = "Baixar modelo CSV do kind")
    fun modelo(@PathVariable kind: String): ResponseEntity<ByteArray> {
        val kindOk = ImportacaoKinds.exigir(kind)
        val bytes = ImportacaoKinds.modeloCsv(kindOk).toByteArray(Charsets.UTF_8)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${kindOk}_modelo.csv\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(bytes)
    }

    @PostMapping(path = ["/{kind}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('import.run')")
    @Operation(summary = "Enviar planilha e agendar validação")
    fun receber(
        @PathVariable kind: String,
        @RequestParam("arquivo") arquivo: MultipartFile,
        authentication: Authentication,
    ): ImportacaoJobResponse =
        ImportacaoJobResponse.de(
            receberImportacaoUseCase.execute(
                principal(authentication).userId,
                kind,
                arquivo.originalFilename ?: "planilha.csv",
                arquivo.bytes,
            ),
            true,
        )

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('import.run')")
    @Operation(summary = "Detalhe e preview da importação")
    fun obter(@PathVariable id: UUID, authentication: Authentication): ImportacaoJobResponse =
        ImportacaoJobResponse.de(
            obterImportacaoUseCase.execute(principal(authentication).userId, id),
            true,
        )

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAuthority('import.run')")
    @Operation(summary = "Confirmar importação validada, em lotes de 1.000")
    fun confirmar(
        @PathVariable id: UUID,
        authentication: Authentication,
        http: HttpServletRequest,
    ): ImportacaoJobResponse =
        ImportacaoJobResponse.de(
            confirmarImportacaoUseCase.execute(principal(authentication).userId, id, clientIp(http)),
            true,
        )

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
