package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.solicitacoes.api.dto.RequestTypeResponse
import br.ufpr.sept.so2.modules.solicitacoes.application.ListarRequestTypesUseCase
import br.ufpr.sept.so2.shared.api.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.function.Function

@RestController
@RequestMapping("/request-types")
@Tag(name = "Tipos de solicitação", description = "RequestType publicados (RF-TR-001 / F1.8)")
class RequestTypeController(
    private val listarRequestTypesUseCase: ListarRequestTypesUseCase,
    private val assembler: SolicitacaoAssembler,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('request.open')")
    @Operation(summary = "Listar tipos elegíveis (publicados)")
    fun listar(@PageableDefault(size = 20) pageable: Pageable): PageResponse<RequestTypeResponse> =
        PageResponse.ofWithLinks(
            listarRequestTypesUseCase.listarPublicados(pageable),
            Function { assembler.from(it) },
        )

    @GetMapping("/{codigo}")
    @PreAuthorize("hasAuthority('request.open')")
    @Operation(summary = "Obter RequestType publicado pelo código")
    fun buscar(@PathVariable("codigo") codigo: String): RequestTypeResponse =
        assembler.from(listarRequestTypesUseCase.buscarPublicado(codigo))
}
