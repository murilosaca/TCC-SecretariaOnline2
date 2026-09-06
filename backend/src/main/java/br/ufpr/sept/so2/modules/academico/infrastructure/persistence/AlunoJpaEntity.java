package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import br.ufpr.sept.so2.modules.academico.domain.Aluno;
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.domain.valueobject.Grr;
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "aluno")
public class AlunoJpaEntity extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(name = "nome_social", length = 200)
    private String nomeSocial;

    @Column(nullable = false, unique = true, length = 11)
    private String grr;

    @Column(name = "email_institucional", nullable = false, unique = true)
    private String emailInstitucional;

    @Column(name = "email_pessoal")
    private String emailPessoal;

    @Column(length = 30)
    private String telefone;

    @Column(name = "id_curso", nullable = false)
    private UUID idCurso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlunoSituacao situacao;

    @Column(nullable = false)
    private boolean ativo;

    protected AlunoJpaEntity() {
    }

    public static AlunoJpaEntity fromDomain(Aluno aluno) {
        AlunoJpaEntity entity = new AlunoJpaEntity();
        entity.setId(aluno.getId());
        entity.merge(aluno);
        return entity;
    }

    public void merge(Aluno aluno) {
        this.nome = aluno.getNome();
        this.nomeSocial = aluno.getNomeSocial();
        this.grr = aluno.getGrr().getValue();
        this.emailInstitucional = aluno.getEmailInstitucional().getValue();
        this.emailPessoal = aluno.getEmailPessoal() == null ? null : aluno.getEmailPessoal().getValue();
        this.telefone = aluno.getTelefone();
        this.idCurso = aluno.getIdCurso();
        this.situacao = aluno.getSituacao();
        this.ativo = aluno.isAtivo();
    }

    public Aluno toDomain() {
        return new Aluno(
                getId(),
                nome,
                nomeSocial,
                Grr.of(grr),
                Email.of(emailInstitucional),
                emailPessoal == null ? null : Email.of(emailPessoal),
                telefone,
                idCurso,
                situacao,
                ativo,
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
