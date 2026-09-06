package br.ufpr.sept.so2.modules.academico.api.dto;

import br.ufpr.sept.so2.modules.academico.domain.Disciplina;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record DisciplinaResponse(
        UUID id,
        UUID idCurso,
        String codigo,
        String nome,
        int periodo,
        int cargaHorariaTotal,
        int creditos,
        boolean ativa,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        @JsonProperty("_links") Map<String, String> links
) {

    public static DisciplinaResponse from(Disciplina disciplina) {
        String base = "/academico/disciplinas/" + disciplina.getId();
        return new DisciplinaResponse(
                disciplina.getId(),
                disciplina.getIdCurso(),
                disciplina.getCodigo(),
                disciplina.getNome(),
                disciplina.getPeriodo(),
                disciplina.getCargaHorariaTotal(),
                disciplina.getCreditos(),
                disciplina.isAtiva(),
                disciplina.getCreatedAt(),
                disciplina.getUpdatedAt(),
                Map.of("self", base, "atualizar", base, "excluir", base)
        );
    }
}
