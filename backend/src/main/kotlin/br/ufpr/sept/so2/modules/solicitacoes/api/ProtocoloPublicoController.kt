package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.solicitacoes.api.dto.ProtocoloPublicoResponse
import br.ufpr.sept.so2.modules.solicitacoes.application.ConsultarProtocoloPublicoUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/publico/protocolos")
@Tag(name = "Protocolo público", description = "Metadados sanitizados (RF-F0-006) — stub sem PDF")
class ProtocoloPublicoController(
    private val consultarProtocoloPublicoUseCase: ConsultarProtocoloPublicoUseCase,
) {

    @GetMapping("/{protocolo}")
    @Operation(summary = "Verificar protocolo sem autenticação")
    fun buscar(@PathVariable("protocolo") protocolo: String): ProtocoloPublicoResponse =
        ProtocoloPublicoResponse.from(consultarProtocoloPublicoUseCase.execute(protocolo))
}
