package br.ufpr.sept.so2.modules.solicitacoes.domain;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Solicitacao {

    public static final String EVENTO_CRIADA = "CRIADA";
    public static final String EVENTO_TRANSICAO = "TRANSICAO";

    private final UUID id;
    private final UUID tipoId;
    private final String tipoCodigo;
    private final String tipoNome;
    private final int tipoVersao;
    private final UUID solicitanteId;
    private final Protocolo protocolo;
    private String estado;
    private String payloadJson;
    private final String formSchemaSnapshot;
    private final String workflowSnapshot;
    private final OffsetDateTime prazoEm;
    private String hashSha256;
    private final List<SolicitacaoEvento> eventos;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Solicitacao(
            UUID id,
            UUID tipoId,
            String tipoCodigo,
            String tipoNome,
            int tipoVersao,
            UUID solicitanteId,
            Protocolo protocolo,
            String estado,
            String payloadJson,
            String formSchemaSnapshot,
            String workflowSnapshot,
            OffsetDateTime prazoEm,
            String hashSha256,
            List<SolicitacaoEvento> eventos,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.tipoId = tipoId;
        this.tipoCodigo = tipoCodigo;
        this.tipoNome = tipoNome;
        this.tipoVersao = tipoVersao;
        this.solicitanteId = solicitanteId;
        this.protocolo = protocolo;
        this.estado = estado;
        this.payloadJson = payloadJson;
        this.formSchemaSnapshot = formSchemaSnapshot;
        this.workflowSnapshot = workflowSnapshot;
        this.prazoEm = prazoEm;
        this.hashSha256 = hashSha256;
        this.eventos = eventos == null ? new ArrayList<>() : new ArrayList<>(eventos);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Solicitacao abrir(
            UUID id,
            UUID eventoId,
            TipoSolicitacao tipo,
            UUID solicitanteId,
            Protocolo protocolo,
            String payloadJson,
            WorkflowDefinicao workflow,
            OffsetDateTime agora
    ) {
        String estadoInicial = workflow.getInicial();
        Solicitacao solicitacao = new Solicitacao(
                id,
                tipo.getId(),
                tipo.getCodigo(),
                tipo.getNome(),
                tipo.getVersao(),
                solicitanteId,
                protocolo,
                estadoInicial,
                payloadJson,
                tipo.getFormSchema(),
                tipo.getWorkflowJson(),
                agora.plusDays(tipo.getPrazoDias()),
                null,
                new ArrayList<>(),
                agora,
                agora
        );
        solicitacao.eventos.add(new SolicitacaoEvento(
                eventoId,
                EVENTO_CRIADA,
                null,
                estadoInicial,
                solicitanteId,
                null,
                payloadJson,
                agora
        ));
        return solicitacao;
    }

    public void transicionar(
            UUID eventoId,
            WorkflowDefinicao workflow,
            String acao,
            UUID atorId,
            String parecer,
            OffsetDateTime agora
    ) {
        String destino = workflow.transicionar(estado, acao);
        String origem = this.estado;
        this.estado = destino;
        this.updatedAt = agora;
        this.eventos.add(new SolicitacaoEvento(
                eventoId,
                EVENTO_TRANSICAO,
                origem,
                destino,
                atorId,
                parecer,
                null,
                agora
        ));
    }

    public boolean pertenceA(UUID usuarioId) {
        return solicitanteId.equals(usuarioId);
    }

    public boolean prazoVencido(OffsetDateTime agora) {
        return prazoEm != null && agora.isAfter(prazoEm);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTipoId() {
        return tipoId;
    }

    public String getTipoCodigo() {
        return tipoCodigo;
    }

    public String getTipoNome() {
        return tipoNome;
    }

    public int getTipoVersao() {
        return tipoVersao;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public Protocolo getProtocolo() {
        return protocolo;
    }

    public String getEstado() {
        return estado;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public String getFormSchemaSnapshot() {
        return formSchemaSnapshot;
    }

    public String getWorkflowSnapshot() {
        return workflowSnapshot;
    }

    public OffsetDateTime getPrazoEm() {
        return prazoEm;
    }

    public String getHashSha256() {
        return hashSha256;
    }

    public List<SolicitacaoEvento> getEventos() {
        return List.copyOf(eventos);
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
