package br.ufpr.sept.so2.modules.academico.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DisciplinaRequest(
        @NotNull UUID idCurso,
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 200) String nome,
        @Min(1) @Max(20) Integer periodo,
        @Positive Integer cargaHorariaTotal,
        @Positive Integer creditos,
        Boolean ativa
) {
}
