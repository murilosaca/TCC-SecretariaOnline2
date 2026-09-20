package br.ufpr.sept.so2.modules.academico.api

import br.ufpr.sept.so2.modules.academico.api.dto.AlunoRequest
import br.ufpr.sept.so2.modules.academico.api.dto.AlunoResponse
import br.ufpr.sept.so2.modules.academico.application.AlunoApplicationService
import br.ufpr.sept.so2.modules.iam.infrastructure.security.IamPrincipal
import br.ufpr.sept.so2.shared.api.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/academico/alunos")
@Tag(name = "Alunos", description = "Cadastro acadêmico de alunos (RF-F5-003)")
class AlunoController(
    private val alunoApplicationService: AlunoApplicationService,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('user.manage_students')")
    @Operation(summary = "Listar alunos do escopo (filtro opcional por curso ou termo)")
    fun listar(
        @RequestParam(required = false) idCurso: UUID?,
        @RequestParam(required = false) termo: String?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<AlunoResponse> {
        val usuarioId = principal(authentication).userId
        val (pagina, cursoIds) = alunoApplicationService.listar(usuarioId, idCurso, termo, pageable)
        return PageResponse.ofWithLinks(
            pagina,
            Function { aluno -> AlunoResponse.from(aluno, aluno.idCurso in cursoIds) },
            mapOf("criar" to "/academico/alunos"),
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user.manage_students')")
    @Operation(summary = "Buscar aluno por id")
    fun buscar(@PathVariable id: UUID, authentication: Authentication): AlunoResponse =
        AlunoResponse.from(alunoApplicationService.buscarPorId(id, principal(authentication).userId), true)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('user.manage_students')")
    @Operation(summary = "Cadastrar aluno")
    fun criar(@Valid @RequestBody request: AlunoRequest, authentication: Authentication): AlunoResponse =
        AlunoResponse.from(
            alunoApplicationService.criar(
                principal(authentication).userId,
                request.nome!!,
                request.nomeSocial,
                request.grr!!,
                request.emailInstitucional!!,
                request.emailPessoal,
                request.telefone,
                request.idCurso!!,
                request.situacao,
            ),
            true,
        )

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user.manage_students')")
    @Operation(summary = "Atualizar dados do aluno")
    fun atualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AlunoRequest,
        authentication: Authentication,
    ): AlunoResponse =
        AlunoResponse.from(
            alunoApplicationService.atualizar(
                id,
                principal(authentication).userId,
                request.nome,
                request.nomeSocial,
                request.emailPessoal,
                request.telefone,
                request.idCurso,
                request.situacao,
                request.ativo,
            ),
            true,
        )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('user.manage_students')")
    @Operation(summary = "Excluir cadastro de aluno")
    fun excluir(@PathVariable id: UUID, authentication: Authentication) {
        alunoApplicationService.excluir(id, principal(authentication).userId)
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
