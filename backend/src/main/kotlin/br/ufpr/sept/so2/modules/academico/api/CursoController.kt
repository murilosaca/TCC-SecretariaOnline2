package br.ufpr.sept.so2.modules.academico.api

import br.ufpr.sept.so2.modules.academico.api.dto.CursoRequest
import br.ufpr.sept.so2.modules.academico.api.dto.CursoResponse
import br.ufpr.sept.so2.modules.academico.application.CursoApplicationService
import br.ufpr.sept.so2.shared.api.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
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

@RestController
@RequestMapping("/academico/cursos")
@Tag(name = "Cursos", description = "Cadastro de cursos (RF-F5-004-a)")
class CursoController(
    private val cursoApplicationService: CursoApplicationService,
) {
    @GetMapping
    @Operation(summary = "Listar cursos paginados")
    fun listar(@PageableDefault(size = 20) pageable: Pageable): PageResponse<CursoResponse> =
        PageResponse.ofWithLinks(cursoApplicationService.listar(pageable), CursoResponse::from)

    @GetMapping("/{id}")
    @Operation(summary = "Buscar curso por id")
    fun buscar(@PathVariable id: UUID): CursoResponse =
        CursoResponse.from(cursoApplicationService.buscarPorId(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar curso")
    fun criar(@Valid @RequestBody request: CursoRequest): CursoResponse {
        val horas = request.horasFormativasMinimas ?: 120
        return CursoResponse.from(
            cursoApplicationService.criar(
                request.nome!!,
                request.sigla!!,
                request.codigo!!,
                request.idCoordenador,
                horas,
            ),
        )
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar curso")
    fun atualizar(@PathVariable id: UUID, @Valid @RequestBody request: CursoRequest): CursoResponse =
        CursoResponse.from(
            cursoApplicationService.atualizar(
                id,
                request.nome,
                request.sigla,
                request.codigo,
                request.idCoordenador,
                request.horasFormativasMinimas,
                request.ativo,
            ),
        )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir curso")
    fun excluir(@PathVariable id: UUID) {
        cursoApplicationService.excluir(id)
    }
}
