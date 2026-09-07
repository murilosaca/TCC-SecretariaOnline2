package br.ufpr.sept.so2.modules.publico.api

import br.ufpr.sept.so2.shared.config.ContatoProperties
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/publico")
@Tag(name = "Público", description = "Endpoints sem autenticação (RF-F0-004)")
class PublicoController(
    private val contatoProperties: ContatoProperties,
) {

    @GetMapping("/contato")
    @Operation(summary = "Informações de contato da secretaria")
    fun contato(): ContatoResponse =
        ContatoResponse(
            contatoProperties.nome,
            contatoProperties.endereco,
            contatoProperties.telefone,
            contatoProperties.email,
            contatoProperties.horario,
        )

    data class ContatoResponse(
        val nome: String?,
        val endereco: String?,
        val telefone: String?,
        val email: String?,
        val horario: String?,
    )
}
