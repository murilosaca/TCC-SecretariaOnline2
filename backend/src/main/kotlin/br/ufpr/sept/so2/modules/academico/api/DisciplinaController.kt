package br.ufpr.sept.so2.modules.academico.api

import br.ufpr.sept.so2.modules.academico.api.dto.DisciplinaRequest
import br.ufpr.sept.so2.modules.academico.api.dto.DisciplinaResponse
import br.ufpr.sept.so2.modules.academico.application.DisciplinaApplicationService
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/academico/disciplinas")
@Tag(name = "Disciplinas", description = "Cadastro de disciplinas vinculadas a cursos (RF-F5-004-b)")
class DisciplinaController(
    private val disciplinaApplicationService: DisciplinaApplicationService,
) {
    @GetMapping
    @Operation(summary = "Listar disciplinas (filtro opcional por curso)")
    fun listar(
        @RequestParam(required = false) idCurso: UUID?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): PageResponse<DisciplinaResponse> =
        PageResponse.ofWithLinks(disciplinaApplicationService.listar(idCurso, pageable), DisciplinaResponse::from)

    @GetMapping("/{id}")
    @Operation(summary = "Buscar disciplina por id")
    fun buscar(@PathVariable id: UUID): DisciplinaResponse =
        DisciplinaResponse.from(disciplinaApplicationService.buscarPorId(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar disciplina")
    fun criar(@Valid @RequestBody request: DisciplinaRequest): DisciplinaResponse =
        DisciplinaResponse.from(
            disciplinaApplicationService.criar(
                request.idCurso!!,
                request.codigo!!,
                request.nome!!,
                request.periodo!!,
                request.cargaHorariaTotal!!,
                request.creditos!!,
            ),
        )

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar disciplina")
    fun atualizar(@PathVariable id: UUID, @Valid @RequestBody request: DisciplinaRequest): DisciplinaResponse =
        DisciplinaResponse.from(
            disciplinaApplicationService.atualizar(
                id,
                request.codigo,
                request.nome,
                request.periodo,
                request.cargaHorariaTotal,
                request.creditos,
                request.ativa,
            ),
        )

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir disciplina")
    fun excluir(@PathVariable id: UUID) {
        disciplinaApplicationService.excluir(id)
    }
}
