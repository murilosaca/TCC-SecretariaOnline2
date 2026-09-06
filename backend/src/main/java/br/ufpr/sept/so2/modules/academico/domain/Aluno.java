package br.ufpr.sept.so2.modules.academico.domain;

import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.domain.valueobject.Grr;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Aluno {

    private final UUID id;
    private String nome;
    private String nomeSocial;
    private Grr grr;
    private Email emailInstitucional;
    private Email emailPessoal;
    private String telefone;
    private UUID idCurso;
    private AlunoSituacao situacao;
    private boolean ativo;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Aluno(
            UUID id,
            String nome,
            String nomeSocial,
            Grr grr,
            Email emailInstitucional,
            Email emailPessoal,
            String telefone,
            UUID idCurso,
            AlunoSituacao situacao,
            boolean ativo,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.nome = nome;
        this.nomeSocial = nomeSocial;
        this.grr = grr;
        this.emailInstitucional = emailInstitucional;
        this.emailPessoal = emailPessoal;
        this.telefone = telefone;
        this.idCurso = idCurso;
        this.situacao = situacao;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void atualizar(
            String nome,
            String nomeSocial,
            Email emailPessoal,
            String telefone,
            UUID idCurso,
            AlunoSituacao situacao,
            Boolean ativo
    ) {
        if (nome != null) {
            this.nome = nome;
        }
        this.nomeSocial = nomeSocial;
        this.emailPessoal = emailPessoal;
        this.telefone = telefone;
        if (idCurso != null) {
            this.idCurso = idCurso;
        }
        if (situacao != null) {
            this.situacao = situacao;
        }
        if (ativo != null) {
            this.ativo = ativo;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNomeSocial() {
        return nomeSocial;
    }

    public Grr getGrr() {
        return grr;
    }

    public Email getEmailInstitucional() {
        return emailInstitucional;
    }

    public Email getEmailPessoal() {
        return emailPessoal;
    }

    public String getTelefone() {
        return telefone;
    }

    public UUID getIdCurso() {
        return idCurso;
    }

    public AlunoSituacao getSituacao() {
        return situacao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
