package br.ufpr.sept.so2.modules.comunicacao.api

import br.ufpr.sept.so2.modules.comunicacao.application.ListarOutboxUseCase
import br.ufpr.sept.so2.modules.comunicacao.application.OutboxDispatcherMonitor.JobCard
import br.ufpr.sept.so2.modules.comunicacao.application.ReentregarOutboxUseCase
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxEvento
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/admin/outbox")
@Tag(name = "Admin · Outbox", description = "F7.6 — observabilidade do dispatcher")
class OutboxAdminController(
    private val listarOutboxUseCase: ListarOutboxUseCase,
    private val reentregarOutboxUseCase: ReentregarOutboxUseCase,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('system.observe')")
    @Operation(summary = "Listar eventos do Outbox e o job do dispatcher")
    fun listar(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) tipo: String?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): OutboxListaResponse {
        val painel = listarOutboxUseCase.execute(status, tipo, pageable)
        return OutboxListaResponse(
            PageResponse.ofWithLinks(painel.pagina, Function { OutboxEventoResponse.from(it) }),
            painel.alertaLatencia,
            painel.jobs.map(JobCardResponse::from),
        )
    }

    @PostMapping("/{id}/reentrega")
    @PreAuthorize("hasAuthority('system.observe')")
    @Operation(summary = "Reentregar evento FAILED ou DEAD")
    fun reentregar(@PathVariable id: UUID, authentication: Authentication): OutboxEventoResponse {
        val principal = authentication.principal as IamPrincipal
        return OutboxEventoResponse.from(reentregarOutboxUseCase.execute(id, principal.userId))
    }
}

data class OutboxListaResponse(
    @get:JsonUnwrapped
    val pagina: PageResponse<OutboxEventoResponse>,
    val alertaLatencia: String?,
    val jobs: List<JobCardResponse>,
)

data class OutboxEventoResponse(
    val id: UUID,
    val tipo: String,
    val payloadResumo: String,
    val status: String,
    val tentativas: Int,
    val createdAt: OffsetDateTime,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
) {
    companion object {
        fun from(evento: OutboxEvento): OutboxEventoResponse {
            val links = linkedMapOf("self" to "/admin/outbox/${evento.id}")
            if (evento.status == "FAILED" || evento.status == "DEAD") {
                links["retry"] = "/admin/outbox/${evento.id}/reentrega"
            }
            return OutboxEventoResponse(
                evento.id,
                evento.tipo,
                resumir(evento.payload),
                evento.status,
                evento.tentativas,
                evento.createdAt,
                links,
            )
        }

        private fun resumir(payload: String): String {
            val texto = payload.replace(Regex("\\s+"), " ").trim()
            return if (texto.length <= 120) texto else texto.take(117) + "..."
        }
    }
}

data class JobCardResponse(
    val nome: String,
    val frequencia: String,
    val ultimoRun: OffsetDateTime?,
    val proximoRun: OffsetDateTime?,
    val status: String,
) {
    companion object {
        fun from(card: JobCard): JobCardResponse =
            JobCardResponse(card.nome, card.frequencia, card.ultimoRun, card.proximoRun, card.status)
    }
}
