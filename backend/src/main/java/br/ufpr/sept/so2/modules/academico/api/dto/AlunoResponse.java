package br.ufpr.sept.so2.modules.academico.api.dto;

import br.ufpr.sept.so2.modules.academico.domain.Aluno;
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record AlunoResponse(
        UUID id,
        String nome,
        String nomeSocial,
        String grr,
        String emailInstitucional,
        String emailPessoal,
        String telefone,
        UUID idCurso,
        AlunoSituacao situacao,
        boolean ativo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        @JsonProperty("_links") Map<String, String> links
) {

    public static AlunoResponse from(Aluno aluno) {
        String base = "/academico/alunos/" + aluno.getId();
        return new AlunoResponse(
                aluno.getId(),
                aluno.getNome(),
                aluno.getNomeSocial(),
                aluno.getGrr().getValue(),
                aluno.getEmailInstitucional().getValue(),
                aluno.getEmailPessoal() == null ? null : aluno.getEmailPessoal().getValue(),
                aluno.getTelefone(),
                aluno.getIdCurso(),
                aluno.getSituacao(),
                aluno.isAtivo(),
                aluno.getCreatedAt(),
                aluno.getUpdatedAt(),
                Map.of("self", base, "atualizar", base, "excluir", base)
        );
    }
}
