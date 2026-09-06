package br.ufpr.sept.so2.modules.academico.api.dto;

import br.ufpr.sept.so2.modules.academico.domain.Curso;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record CursoResponse(
        UUID id,
        String nome,
        String sigla,
        String codigo,
        UUID idCoordenador,
        int horasFormativasMinimas,
        boolean ativo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        @JsonProperty("_links") Map<String, String> links
) {

    public static CursoResponse from(Curso curso) {
        String base = "/academico/cursos/" + curso.getId();
        return new CursoResponse(
                curso.getId(),
                curso.getNome(),
                curso.getSigla(),
                curso.getCodigo(),
                curso.getIdCoordenador(),
                curso.getHorasFormativasMinimas(),
                curso.isAtivo(),
                curso.getCreatedAt(),
                curso.getUpdatedAt(),
                Map.of(
                        "self", base,
                        "atualizar", base,
                        "excluir", base,
                        "disciplinas", base + "/disciplinas"
                )
        );
    }
}
