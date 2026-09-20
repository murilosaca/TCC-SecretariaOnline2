package br.ufpr.sept.so2.modules.academico.api

import br.ufpr.sept.so2.modules.academico.api.dto.DisciplinaRequest
import br.ufpr.sept.so2.modules.academico.api.dto.DisciplinaResponse
import br.ufpr.sept.so2.modules.academico.application.DisciplinaApplicationService
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
@RequestMapping("/academico/disciplinas")
@Tag(name = "Disciplinas", description = "Cadastro de disciplinas vinculadas a cursos (RF-F5-004-b)")
class DisciplinaController(
    private val disciplinaApplicationService: DisciplinaApplicationService,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('subject.manage')")
    @Operation(summary = "Listar disciplinas do escopo")
    fun listar(
        @RequestParam(required = false) idCurso: UUID?,
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<DisciplinaResponse> {
        val usuarioId = principal(authentication).userId
        val cursoIds = disciplinaApplicationService.cursoIdsDoUsuario(usuarioId)
        return PageResponse.ofWithLinks(
            disciplinaApplicationService.listar(usuarioId, idCurso, pageable),
            Function { disciplina -> DisciplinaResponse.from(disciplina, disciplina.idCurso in cursoIds) },
            mapOf("criar" to "/academico/disciplinas"),
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('subject.manage')")
    @Operation(summary = "Buscar disciplina por id")
    fun buscar(@PathVariable id: UUID, authentication: Authentication): DisciplinaResponse =
        DisciplinaResponse.from(disciplinaApplicationService.buscarPorId(id, principal(authentication).userId), true)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('subject.manage')")
    @Operation(summary = "Criar disciplina")
    fun criar(@Valid @RequestBody request: DisciplinaRequest, authentication: Authentication): DisciplinaResponse =
        DisciplinaResponse.from(
            disciplinaApplicationService.criar(
                principal(authentication).userId,
                request.idCurso!!,
                request.codigo!!,
                request.nome!!,
                request.periodo!!,
                request.cargaHorariaTotal!!,
                request.creditos!!,
            ),
            true,
        )

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('subject.manage')")
    @Operation(summary = "Atualizar disciplina")
    fun atualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody request: DisciplinaRequest,
        authentication: Authentication,
    ): DisciplinaResponse =
        DisciplinaResponse.from(
            disciplinaApplicationService.atualizar(
                id,
                principal(authentication).userId,
                request.codigo,
                request.nome,
                request.periodo,
                request.cargaHorariaTotal,
                request.creditos,
                request.ativa,
            ),
            true,
        )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('subject.manage')")
    @Operation(summary = "Excluir disciplina")
    fun excluir(@PathVariable id: UUID, authentication: Authentication) {
        disciplinaApplicationService.excluir(id, principal(authentication).userId)
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
