package br.ufpr.sept.so2.modules.academico.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CursoRequest(
        @NotBlank @Size(max = 200) String nome,
        @NotBlank @Size(max = 20) String sigla,
        @NotBlank @Size(max = 30) String codigo,
        UUID idCoordenador,
        @Min(0) Integer horasFormativasMinimas,
        Boolean ativo
) {
}
