package br.ufpr.sept.so2.modules.solicitacoes.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TipoSolicitacao {

    public static final String DRAFT = "DRAFT";
    public static final String PUBLISHED = "PUBLISHED";

    private final UUID id;
    private final String codigo;
    private String nome;
    private String descricao;
    private String status;
    private String formSchema;
    private String workflowJson;
    private int prazoDias;
    private int versao;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public TipoSolicitacao(
            UUID id,
            String codigo,
            String nome,
            String descricao,
            String status,
            String formSchema,
            String workflowJson,
            int prazoDias,
            int versao,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.status = status;
        this.formSchema = formSchema;
        this.workflowJson = workflowJson;
        this.prazoDias = prazoDias;
        this.versao = versao;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean isPublished() {
        return PUBLISHED.equals(status);
    }

    public UUID getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getStatus() {
        return status;
    }

    public String getFormSchema() {
        return formSchema;
    }

    public String getWorkflowJson() {
        return workflowJson;
    }

    public int getPrazoDias() {
        return prazoDias;
    }

    public int getVersao() {
        return versao;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
