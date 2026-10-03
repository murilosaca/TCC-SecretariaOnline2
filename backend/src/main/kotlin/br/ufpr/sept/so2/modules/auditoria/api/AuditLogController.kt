package br.ufpr.sept.so2.modules.auditoria.api

import br.ufpr.sept.so2.modules.auditoria.api.dto.AuditLogItemResponse
import br.ufpr.sept.so2.modules.auditoria.application.AuditLogPagina
import br.ufpr.sept.so2.modules.auditoria.application.ListarAuditLogUseCase
import br.ufpr.sept.so2.shared.api.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/audit-log")
@Tag(name = "Auditoria", description = "Leitura imutável da trilha (RF-F7-006)")
class AuditLogController(
    private val listarAuditLogUseCase: ListarAuditLogUseCase,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('audit.read')")
    @Operation(summary = "Pesquisar a trilha de auditoria")
    fun listar(
        @RequestParam(required = false) ator: String?,
        @RequestParam(required = false) acao: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) de: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) ate: LocalDate?,
        @PageableDefault(size = ListarAuditLogUseCase.TAMANHO_PAGINA) pageable: Pageable,
    ): PageResponse<AuditLogItemResponse> {
        val pagina = listarAuditLogUseCase.execute(ator, acao, de, ate, pageable)
        return PageResponse.of(paginaSpring(pagina))
    }

    private fun paginaSpring(pagina: AuditLogPagina) =
        PageImpl(
            pagina.itens.map(AuditLogItemResponse::from),
            PageRequest.of(pagina.numero, pagina.tamanho),
            pagina.total,
        )
}
