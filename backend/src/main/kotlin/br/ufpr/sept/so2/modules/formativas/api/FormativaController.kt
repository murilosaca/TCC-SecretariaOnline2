package br.ufpr.sept.so2.modules.formativas.api

import br.ufpr.sept.so2.modules.formativas.api.dto.FormativaResponse
import br.ufpr.sept.so2.modules.formativas.application.CancelarFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.application.ConfirmarFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.application.ListarMinhasFormativasUseCase
import br.ufpr.sept.so2.modules.formativas.application.ObterFormativaUseCase
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
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
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/formativas")
@Tag(name = "Formativas", description = "Atividades formativas a partir de presença validada (RF-F1-006)")
class FormativaController(
    private val listarMinhasFormativasUseCase: ListarMinhasFormativasUseCase,
    private val obterFormativaUseCase: ObterFormativaUseCase,
    private val confirmarFormativaUseCase: ConfirmarFormativaUseCase,
    private val cancelarFormativaUseCase: CancelarFormativaUseCase,
    private val alunoPorUsuarioPort: AlunoPorUsuarioPort,
    private val assembler: FormativaAssembler,
) {

    @GetMapping
    @PreAuthorize("hasAuthority('formative.view_own')")
    @Operation(summary = "Listar formativas do aluno autenticado")
    fun listar(
        @RequestParam(defaultValue = "me") audience: String,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<FormativaResponse> {
        val principal = principal(authentication)
        val alunoId = alunoId(principal.userId)
        return PageResponse.ofWithLinks(
            listarMinhasFormativasUseCase.execute(principal.userId, audience, pageable),
            Function { item -> assembler.from(item, alunoId, principal.authorities) },
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('formative.view_own')")
    @Operation(summary = "Detalhe da formativa")
    fun buscar(@PathVariable id: UUID, authentication: Authentication): FormativaResponse {
        val principal = principal(authentication)
        return assembler.from(
            obterFormativaUseCase.execute(id, principal.userId),
            alunoId(principal.userId),
            principal.authorities,
        )
    }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAuthority('formative.confirm_own')")
    @Operation(summary = "Confirmar formativa pré-validada por presença")
    fun confirmar(@PathVariable id: UUID, authentication: Authentication): FormativaResponse {
        val principal = principal(authentication)
        return assembler.from(
            confirmarFormativaUseCase.execute(id, principal.userId),
            alunoId(principal.userId),
            principal.authorities,
        )
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('formative.view_own')")
    @Operation(summary = "Cancelar formativa pendente de confirmação")
    fun cancelar(@PathVariable id: UUID, authentication: Authentication): FormativaResponse {
        val principal = principal(authentication)
        return assembler.from(
            cancelarFormativaUseCase.execute(id, principal.userId),
            alunoId(principal.userId),
            principal.authorities,
        )
    }

    private fun alunoId(usuarioId: UUID): UUID =
        alunoPorUsuarioPort.resolver(usuarioId)?.id
            ?: throw AcessoNegadoException("Cadastro acadêmico de aluno não encontrado.")

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
