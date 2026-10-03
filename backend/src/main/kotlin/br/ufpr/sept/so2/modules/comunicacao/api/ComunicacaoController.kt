package br.ufpr.sept.so2.modules.comunicacao.api

import br.ufpr.sept.so2.modules.comunicacao.api.dto.AudienciasResponse
import br.ufpr.sept.so2.modules.comunicacao.api.dto.BadgesResponse
import br.ufpr.sept.so2.modules.comunicacao.api.dto.ComunicacaoAssembler
import br.ufpr.sept.so2.modules.comunicacao.api.dto.ComunicacaoItemResponse
import br.ufpr.sept.so2.modules.comunicacao.api.dto.ComunicacaoPublicadaResponse
import br.ufpr.sept.so2.modules.comunicacao.api.dto.PublicarComunicacaoRequest
import br.ufpr.sept.so2.modules.comunicacao.application.ListarAudienciasUseCase
import br.ufpr.sept.so2.modules.comunicacao.application.ListarComunicacoesUseCase
import br.ufpr.sept.so2.modules.comunicacao.application.MarcarComunicacaoLidaUseCase
import br.ufpr.sept.so2.modules.comunicacao.application.PublicarComunicacaoComando
import br.ufpr.sept.so2.modules.comunicacao.application.PublicarComunicacaoUseCase
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID
import java.util.function.Function

data class HubComunicacaoResponse(
    val content: List<ComunicacaoItemResponse>,
    val badges: BadgesResponse,
    val page: PageResponse.PageMeta,
    @get:com.fasterxml.jackson.annotation.JsonProperty("_links")
    val links: PageResponse.PageLinks?,
)

@RestController
@RequestMapping("/communications")
@Tag(name = "Comunicações", description = "Hub F1.6 e publicação F3.8")
class ComunicacaoController(
    private val listarUseCase: ListarComunicacoesUseCase,
    private val marcarUseCase: MarcarComunicacaoLidaUseCase,
    private val publicarUseCase: PublicarComunicacaoUseCase,
    private val audienciasUseCase: ListarAudienciasUseCase,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('communication.read')")
    @Operation(summary = "Listar comunicações do usuário autenticado")
    fun listar(
        @RequestParam(defaultValue = "TODOS") aba: String,
        @RequestParam(required = false) tipo: String?,
        @RequestParam(required = false) lido: Boolean?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): HubComunicacaoResponse {
        val lista = listarUseCase.execute(principal(authentication).userId, aba, tipo, lido, pageable)
        val content = lista.itens.map { ComunicacaoAssembler.item(it) }
        val envelope = PageResponse.ofWithLinks(
            PageImpl(content, org.springframework.data.domain.PageRequest.of(lista.page, lista.size), lista.total),
            Function.identity(),
        )
        return HubComunicacaoResponse(envelope.content, BadgesResponse.from(lista.badges), envelope.page, envelope.links)
    }

    @GetMapping("/audiencias")
    @PreAuthorize("hasAuthority('communication.publish_class')")
    @Operation(summary = "Turmas e cursos do professor autenticado")
    fun audiencias(authentication: Authentication): AudienciasResponse =
        AudienciasResponse(
            audienciasUseCase.execute(principal(authentication).userId).map { ComunicacaoAssembler.audiencia(it) },
        )

    @PostMapping
    @PreAuthorize("hasAuthority('communication.publish_class')")
    @Operation(summary = "Publicar comunicado para turma ou curso")
    fun publicar(
        @RequestBody body: PublicarComunicacaoRequest,
        authentication: Authentication,
    ): ResponseEntity<ComunicacaoPublicadaResponse> {
        val criada = publicarUseCase.execute(
            principal(authentication).userId,
            PublicarComunicacaoComando(
                body.titulo,
                body.corpo,
                body.audiencia?.tipo,
                body.audiencia?.id,
                body.prioridade,
                body.expiraEm,
            ),
        )
        val resposta = ComunicacaoAssembler.publicada(criada)
        return ResponseEntity.created(URI.create("/communications/${criada.id}")).body(resposta)
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAuthority('communication.read')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marcar comunicação como lida")
    fun marcarLida(@PathVariable id: UUID, authentication: Authentication) {
        marcarUseCase.execute(id, principal(authentication).userId)
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
