package br.ufpr.sept.so2.modules.academico.api

import br.ufpr.sept.so2.modules.academico.api.dto.CursoRequest
import br.ufpr.sept.so2.modules.academico.api.dto.CursoResponse
import br.ufpr.sept.so2.modules.academico.application.CursoApplicationService
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import java.util.function.Function

@RestController
@RequestMapping("/academico/cursos")
@Tag(name = "Cursos", description = "Cadastro de cursos (RF-F5-004-a)")
class CursoController(
    private val cursoApplicationService: CursoApplicationService,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('course.manage')")
    @Operation(summary = "Listar cursos do escopo (secretário ∪ coordenador)")
    fun listar(
        @PageableDefault(size = 20) pageable: Pageable,
        authentication: Authentication,
    ): PageResponse<CursoResponse> {
        val usuarioId = principal(authentication).userId
        val (pagina, cursoIds) = cursoApplicationService.listar(usuarioId, pageable)
        val secretarios = cursoApplicationService.secretariosPorCursos(pagina.content.map { it.id })
        return PageResponse.ofWithLinks(
            pagina,
            Function { curso ->
                CursoResponse.from(
                    curso,
                    secretarios[curso.id].orEmpty(),
                    curso.id in cursoIds,
                )
            },
            mapOf("criar" to "/academico/cursos"),
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('course.manage')")
    @Operation(summary = "Buscar curso por id")
    fun buscar(@PathVariable id: UUID, authentication: Authentication): CursoResponse {
        val usuarioId = principal(authentication).userId
        val curso = cursoApplicationService.buscarPorId(id, usuarioId)
        return CursoResponse.from(
            curso,
            cursoApplicationService.secretariosIds(curso.id),
            cursoApplicationService.estaNoEscopo(usuarioId, curso.id),
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('course.manage')")
    @Operation(summary = "Criar curso")
    fun criar(@Valid @RequestBody request: CursoRequest, authentication: Authentication): CursoResponse {
        val usuarioId = principal(authentication).userId
        val horas = request.horasFormativasMinimas ?: 120
        val curso = cursoApplicationService.criar(
            usuarioId,
            request.nome!!,
            request.sigla!!,
            request.codigo!!,
            request.idCoordenador,
            horas,
            request.secretariosIds,
        )
        return CursoResponse.from(
            curso,
            cursoApplicationService.secretariosIds(curso.id),
            cursoApplicationService.estaNoEscopo(usuarioId, curso.id),
        )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('course.manage')")
    @Operation(summary = "Atualizar curso")
    fun atualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CursoRequest,
        authentication: Authentication,
    ): CursoResponse {
        val usuarioId = principal(authentication).userId
        val curso = cursoApplicationService.atualizar(
            id,
            usuarioId,
            request.nome,
            request.sigla,
            request.codigo,
            request.idCoordenador,
            request.horasFormativasMinimas,
            request.ativo,
            request.secretariosIds,
        )
        return CursoResponse.from(
            curso,
            cursoApplicationService.secretariosIds(curso.id),
            cursoApplicationService.estaNoEscopo(usuarioId, curso.id),
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('course.manage')")
    @Operation(summary = "Excluir curso")
    fun excluir(@PathVariable id: UUID, authentication: Authentication) {
        cursoApplicationService.excluir(id, principal(authentication).userId)
    }

    companion object {
        private fun principal(authentication: Authentication): IamPrincipal =
            authentication.principal as IamPrincipal
    }
}
