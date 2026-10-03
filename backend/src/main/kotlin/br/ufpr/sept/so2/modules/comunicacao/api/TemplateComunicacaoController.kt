package br.ufpr.sept.so2.modules.comunicacao.api

import br.ufpr.sept.so2.modules.comunicacao.application.ConsultarTemplateComunicacaoUseCase
import br.ufpr.sept.so2.modules.comunicacao.application.CriarTemplateComunicacaoUseCase
import br.ufpr.sept.so2.modules.comunicacao.application.PlaceholdersTemplate
import br.ufpr.sept.so2.modules.comunicacao.application.RevisaoItem
import br.ufpr.sept.so2.modules.comunicacao.application.SalvarRevisaoTemplateUseCase
import br.ufpr.sept.so2.modules.comunicacao.application.TemplateDetalhe
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
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
import java.time.OffsetDateTime
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/admin/templates-comunicacao")
@Tag(name = "Admin · Templates de comunicação", description = "F7.5 — Markdown versionado")
class TemplateComunicacaoController(
    private val consultar: ConsultarTemplateComunicacaoUseCase,
    private val criar: CriarTemplateComunicacaoUseCase,
    private val salvar: SalvarRevisaoTemplateUseCase,
    private val usuarioRepository: UsuarioRepository,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('communication.manage_templates')")
    fun listar(@PageableDefault(size = 20) pageable: Pageable): PageResponse<TemplateResumoResponse> =
        PageResponse.ofWithLinks(
            consultar.listar(pageable),
            Function { TemplateResumoResponse(it.id, it.nome, it.assunto) },
            mapOf("criar" to "/admin/templates-comunicacao"),
        )

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('communication.manage_templates')")
    fun buscar(@PathVariable id: UUID): TemplateComunicacaoResponse =
        resposta(consultar.buscar(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('communication.manage_templates')")
    fun criar(
        @RequestBody request: TemplateComunicacaoRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): TemplateComunicacaoResponse {
        val principal = principal(authentication)
        val criado = criar.execute(request.nome, request.assunto, request.corpo, principal.userId, http.remoteAddr)
        return resposta(criado)
    }

    @PostMapping("/{id}/revisoes")
    @PreAuthorize("hasAuthority('communication.manage_templates')")
    fun salvarRevisao(
        @PathVariable id: UUID,
        @RequestBody request: TemplateComunicacaoRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): TemplateComunicacaoResponse {
        val principal = principal(authentication)
        val salvo = salvar.execute(id, request.assunto, request.corpo, principal.userId, http.remoteAddr)
        return resposta(salvo)
    }

    private fun resposta(detalhe: TemplateDetalhe): TemplateComunicacaoResponse {
        val nomes = detalhe.revisoes.map { it.autorId }.distinct().associateWith { autorId ->
            usuarioRepository.findById(autorId).map { it.nome }.orElse(null)
        }
        return TemplateComunicacaoResponse(
            detalhe.id,
            detalhe.nome,
            detalhe.assunto,
            detalhe.corpo,
            PlaceholdersTemplate.NOMES,
            detalhe.revisoes.map { revisao(it, nomes[it.autorId]) },
            linkedMapOf(
                "self" to "/admin/templates-comunicacao/${detalhe.id}",
                "salvar" to "/admin/templates-comunicacao/${detalhe.id}/revisoes",
            ),
        )
    }

    private fun revisao(item: RevisaoItem, autorNome: String?) = RevisaoTemplateResponse(
        item.versao,
        item.assunto,
        item.corpo,
        item.autorId,
        autorNome,
        item.criadoEm,
        item.status,
    )

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}

data class TemplateComunicacaoRequest(
    val nome: String? = null,
    val assunto: String? = null,
    val corpo: String? = null,
)

data class TemplateResumoResponse(
    val id: UUID,
    val nome: String,
    val assunto: String,
)

data class RevisaoTemplateResponse(
    val versao: Int,
    val assunto: String,
    val corpo: String,
    val autorId: UUID,
    val autorNome: String?,
    val criadoEm: OffsetDateTime,
    val status: String,
)

data class TemplateComunicacaoResponse(
    val id: UUID,
    val nome: String,
    val assunto: String,
    val corpo: String,
    val variaveis: List<String>,
    val revisoes: List<RevisaoTemplateResponse>,
    @get:JsonProperty("_links")
    val links: Map<String, String>,
)
