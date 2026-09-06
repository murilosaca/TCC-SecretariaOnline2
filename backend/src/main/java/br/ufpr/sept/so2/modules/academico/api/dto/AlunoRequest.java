package br.ufpr.sept.so2.modules.academico.api.dto;

import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AlunoRequest(
        @NotBlank @Size(max = 200) String nome,
        @Size(max = 200) String nomeSocial,
        @NotBlank String grr,
        @NotBlank String emailInstitucional,
        String emailPessoal,
        @Size(max = 30) String telefone,
        @NotNull UUID idCurso,
        AlunoSituacao situacao,
        Boolean ativo
) {
}
