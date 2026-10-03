package br.ufpr.sept.so2.modules.importacao.api

import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.modules.importacao.api.dto.DownloadUrlResponse
import br.ufpr.sept.so2.modules.importacao.api.dto.ExportacaoJobResponse
import br.ufpr.sept.so2.modules.importacao.api.dto.ExportacaoKindResponse
import br.ufpr.sept.so2.modules.importacao.application.ConsultarExportacaoUseCase
import br.ufpr.sept.so2.modules.importacao.application.SolicitarExportacaoUseCase
import br.ufpr.sept.so2.modules.importacao.domain.ExportacaoKinds
import br.ufpr.sept.so2.shared.api.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/exportacoes")
@Tag(name = "Exportações", description = "Jobs assíncronos da secretaria (RF-F5-010)")
class ExportacaoController(
    private val solicitarExportacaoUseCase: SolicitarExportacaoUseCase,
    private val consultarExportacaoUseCase: ConsultarExportacaoUseCase,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('export.run')")
    @Operation(summary = "Catálogo e histórico de exportações")
    fun listar(
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): ExportacoesResponse {
        val pagina = consultarExportacaoUseCase.listar(principal(authentication).userId, pageable)
        val page = PageResponse.of(pagina.map(ExportacaoJobResponse::de))
        return ExportacoesResponse(
            kinds = ExportacaoKinds.TODOS.map { ExportacaoKindResponse(it, ExportacaoKinds.titulo(it)) },
            content = page.content,
            page = page.page,
            links = mapOf("self" to "/exportacoes"),
        )
    }

    @PostMapping("/{kind}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAuthority('export.run')")
    @Operation(summary = "Solicitar exportação assíncrona")
    fun solicitar(
        @PathVariable kind: String,
        @RequestBody(required = false) filtros: Map<String, String?>?,
        authentication: Authentication,
    ): ExportacaoJobResponse =
        ExportacaoJobResponse.de(
            solicitarExportacaoUseCase.execute(
                principal(authentication).userId,
                kind,
                filtros ?: emptyMap(),
            ),
        )

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('export.run')")
    fun obter(@PathVariable id: UUID, authentication: Authentication): ExportacaoJobResponse =
        ExportacaoJobResponse.de(consultarExportacaoUseCase.obter(principal(authentication).userId, id))

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('export.run')")
    @Operation(summary = "URL pré-assinada de download")
    fun download(@PathVariable id: UUID, authentication: Authentication): DownloadUrlResponse =
        DownloadUrlResponse(
            consultarExportacaoUseCase.url(principal(authentication).userId, id),
            900,
        )

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}

data class ExportacoesResponse(
    val kinds: List<ExportacaoKindResponse>,
    val content: List<ExportacaoJobResponse>,
    val page: br.ufpr.sept.so2.shared.api.PageResponse.PageMeta,
    @get:com.fasterxml.jackson.annotation.JsonProperty("_links")
    val links: Map<String, String>,
)
