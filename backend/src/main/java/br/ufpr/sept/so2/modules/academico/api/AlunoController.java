package br.ufpr.sept.so2.modules.academico.api;

import br.ufpr.sept.so2.modules.academico.api.dto.AlunoRequest;
import br.ufpr.sept.so2.modules.academico.api.dto.AlunoResponse;
import br.ufpr.sept.so2.modules.academico.application.AlunoApplicationService;
import br.ufpr.sept.so2.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/academico/alunos")
@Tag(name = "Alunos", description = "Cadastro acadêmico de alunos (RF-F5-003)")
public class AlunoController {

    private final AlunoApplicationService alunoApplicationService;

    public AlunoController(AlunoApplicationService alunoApplicationService) {
        this.alunoApplicationService = alunoApplicationService;
    }

    @GetMapping
    @Operation(summary = "Listar alunos com filtro por curso ou termo (nome, GRR, e-mail)")
    public PageResponse<AlunoResponse> listar(
            @RequestParam(required = false) UUID idCurso,
            @RequestParam(required = false) String termo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return PageResponse.ofWithLinks(alunoApplicationService.listar(idCurso, termo, pageable), AlunoResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar aluno por id")
    public AlunoResponse buscar(@PathVariable UUID id) {
        return AlunoResponse.from(alunoApplicationService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar aluno")
    public AlunoResponse criar(@Valid @RequestBody AlunoRequest request) {
        return AlunoResponse.from(alunoApplicationService.criar(
                request.nome(),
                request.nomeSocial(),
                request.grr(),
                request.emailInstitucional(),
                request.emailPessoal(),
                request.telefone(),
                request.idCurso(),
                request.situacao()
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do aluno")
    public AlunoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AlunoRequest request) {
        return AlunoResponse.from(alunoApplicationService.atualizar(
                id,
                request.nome(),
                request.nomeSocial(),
                request.emailPessoal(),
                request.telefone(),
                request.idCurso(),
                request.situacao(),
                request.ativo()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir cadastro de aluno")
    public void excluir(@PathVariable UUID id) {
        alunoApplicationService.excluir(id);
    }
}
