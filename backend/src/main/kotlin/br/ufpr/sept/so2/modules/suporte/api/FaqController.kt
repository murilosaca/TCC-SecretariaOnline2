package br.ufpr.sept.so2.modules.suporte.api

import br.ufpr.sept.so2.modules.suporte.application.ListarFaqUseCase
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/suporte/faq")
@Tag(name = "Suporte", description = "F8.2 — FAQ de leitura")
class FaqController(
    private val listarFaq: ListarFaqUseCase,
) {
    @GetMapping
    fun listar(): List<FaqResponse> =
        listarFaq.execute().map { FaqResponse(it.id, it.pergunta, it.resposta) }
}

data class FaqResponse(
    val id: UUID,
    val pergunta: String,
    val resposta: String,
)
