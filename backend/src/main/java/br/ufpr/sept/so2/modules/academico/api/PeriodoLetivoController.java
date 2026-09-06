package br.ufpr.sept.so2.modules.academico.api;

import br.ufpr.sept.so2.modules.academico.api.dto.PeriodoLetivoRequest;
import br.ufpr.sept.so2.modules.academico.api.dto.PeriodoLetivoResponse;
import br.ufpr.sept.so2.modules.academico.application.PeriodoLetivoApplicationService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/academico/periodos")
@Tag(name = "Períodos letivos", description = "Calendário acadêmico (RF-F5-004-c)")
public class PeriodoLetivoController {

    private final PeriodoLetivoApplicationService periodoLetivoApplicationService;

    public PeriodoLetivoController(PeriodoLetivoApplicationService periodoLetivoApplicationService) {
        this.periodoLetivoApplicationService = periodoLetivoApplicationService;
    }

    @GetMapping
    @Operation(summary = "Listar períodos letivos paginados")
    public PageResponse<PeriodoLetivoResponse> listar(@PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.ofWithLinks(periodoLetivoApplicationService.listar(pageable), PeriodoLetivoResponse::from);
    }

    @GetMapping("/vigente")
    @Operation(summary = "Obter o período letivo vigente hoje")
    public PeriodoLetivoResponse vigente() {
        return PeriodoLetivoResponse.from(periodoLetivoApplicationService.buscarVigente(LocalDate.now()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar período letivo por id")
    public PeriodoLetivoResponse buscar(@PathVariable UUID id) {
        return PeriodoLetivoResponse.from(periodoLetivoApplicationService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar período letivo")
    public PeriodoLetivoResponse criar(@Valid @RequestBody PeriodoLetivoRequest request) {
        return PeriodoLetivoResponse.from(periodoLetivoApplicationService.criar(
                request.ano(),
                request.semestre(),
                request.inicio(),
                request.fim()
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar período letivo")
    public PeriodoLetivoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody PeriodoLetivoRequest request) {
        return PeriodoLetivoResponse.from(periodoLetivoApplicationService.atualizar(
                id,
                request.ano(),
                request.semestre(),
                request.inicio(),
                request.fim(),
                request.ativo()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir período letivo")
    public void excluir(@PathVariable UUID id) {
        periodoLetivoApplicationService.excluir(id);
    }
}
