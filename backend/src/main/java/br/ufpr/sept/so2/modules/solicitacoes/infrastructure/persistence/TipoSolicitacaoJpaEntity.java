package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence;

import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao;
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tipo_solicitacao")
public class TipoSolicitacaoJpaEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 80)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(nullable = false, length = 20)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_schema", nullable = false, columnDefinition = "jsonb")
    private String formSchema;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "workflow_json", nullable = false, columnDefinition = "jsonb")
    private String workflowJson;

    @Column(name = "prazo_dias", nullable = false)
    private int prazoDias;

    @Column(nullable = false)
    private int versao;

    protected TipoSolicitacaoJpaEntity() {
    }

    public static TipoSolicitacaoJpaEntity fromDomain(TipoSolicitacao tipo) {
        TipoSolicitacaoJpaEntity entity = new TipoSolicitacaoJpaEntity();
        entity.setId(tipo.getId());
        entity.merge(tipo);
        return entity;
    }

    public void merge(TipoSolicitacao tipo) {
        this.codigo = tipo.getCodigo();
        this.nome = tipo.getNome();
        this.descricao = tipo.getDescricao();
        this.status = tipo.getStatus();
        this.formSchema = tipo.getFormSchema();
        this.workflowJson = tipo.getWorkflowJson();
        this.prazoDias = tipo.getPrazoDias();
        this.versao = tipo.getVersao();
    }

    public TipoSolicitacao toDomain() {
        return new TipoSolicitacao(
                getId(),
                codigo,
                nome,
                descricao,
                status,
                formSchema,
                workflowJson,
                prazoDias,
                versao,
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
