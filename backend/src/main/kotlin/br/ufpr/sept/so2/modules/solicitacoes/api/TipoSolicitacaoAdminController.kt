package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.solicitacoes.api.dto.RequestTypeResponse
import br.ufpr.sept.so2.modules.solicitacoes.application.CriarTipoSolicitacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.ExcluirTipoSolicitacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.ListarTiposSolicitacaoAdminUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.PublicarTipoSolicitacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.SalvarRascunhoTipoSolicitacaoUseCase
import br.ufpr.sept.so2.modules.solicitacoes.application.TipoEditor
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/admin/tipos-solicitacao")
@Tag(name = "Admin · Tipos de solicitação", description = "F7.4 — editor de RequestType")
class TipoSolicitacaoAdminController(
    private val listar: ListarTiposSolicitacaoAdminUseCase,
    private val criar: CriarTipoSolicitacaoUseCase,
    private val salvar: SalvarRascunhoTipoSolicitacaoUseCase,
    private val publicar: PublicarTipoSolicitacaoUseCase,
    private val excluir: ExcluirTipoSolicitacaoUseCase,
    private val assembler: SolicitacaoAssembler,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('request_type.manage')")
    @Operation(summary = "Listar tipos DRAFT e PUBLISHED")
    fun listar(@PageableDefault(size = 20) pageable: Pageable): PageResponse<RequestTypeResponse> =
        PageResponse.ofWithLinks(
            listar.execute(pageable),
            Function { resposta(it) },
            mapOf("criar" to "/admin/tipos-solicitacao"),
        )

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('request_type.manage')")
    fun buscar(@PathVariable id: UUID): RequestTypeResponse = resposta(listar.buscar(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('request_type.manage')")
    fun criar(
        @RequestBody request: TipoSolicitacaoWriteRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): RequestTypeResponse {
        val codigo = request.codigo ?: throw DadoInvalidoException("Código é obrigatório.")
        val nome = request.nome ?: throw DadoInvalidoException("Nome é obrigatório.")
        val salvo = criar.execute(
            codigo,
            nome,
            request.descricao,
            request.prazoDias ?: 15,
            request.formSchema ?: "",
            request.workflowJson ?: "",
            principal(authentication).userId,
            http.remoteAddr,
        )
        return resposta(listar.buscar(salvo.id))
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('request_type.manage')")
    fun salvarRascunho(
        @PathVariable id: UUID,
        @RequestBody request: TipoSolicitacaoWriteRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): RequestTypeResponse {
        val salvo = salvar.execute(
            id,
            request.nome,
            request.descricao,
            request.prazoDias,
            request.formSchema ?: "",
            request.workflowJson ?: "",
            principal(authentication).userId,
            http.remoteAddr,
        )
        return resposta(listar.buscar(salvo.id))
    }

    @PostMapping("/{id}/publicar")
    @PreAuthorize("hasAuthority('request_type.manage')")
    fun publicar(
        @PathVariable id: UUID,
        @RequestBody request: TipoSolicitacaoWriteRequest,
        authentication: Authentication,
        http: HttpServletRequest,
    ): RequestTypeResponse {
        val salvo = publicar.execute(
            id,
            request.formSchema ?: "",
            request.workflowJson ?: "",
            principal(authentication).userId,
            http.remoteAddr,
        )
        return resposta(listar.buscar(salvo.id))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('request_type.manage')")
    fun excluir(
        @PathVariable id: UUID,
        authentication: Authentication,
        http: HttpServletRequest,
    ) {
        excluir.execute(id, principal(authentication).userId, http.remoteAddr)
    }

    private fun resposta(editor: TipoEditor): RequestTypeResponse {
        val base = assembler.from(editor.tipo)
        val links = linkedMapOf<String, String>()
        links.putAll(base.links)
        links["self"] = "/admin/tipos-solicitacao/${editor.tipo.id}"
        links["publicar"] = "/admin/tipos-solicitacao/${editor.tipo.id}/publicar"
        if (editor.podeSalvar) {
            links["salvar"] = "/admin/tipos-solicitacao/${editor.tipo.id}"
        }
        if (editor.podeExcluir) {
            links["delete"] = "/admin/tipos-solicitacao/${editor.tipo.id}"
        }
        return base.copy(links = links)
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}

data class TipoSolicitacaoWriteRequest(
    val codigo: String? = null,
    val nome: String? = null,
    val descricao: String? = null,
    val prazoDias: Int? = null,
    val formSchema: String? = null,
    val workflowJson: String? = null,
)
