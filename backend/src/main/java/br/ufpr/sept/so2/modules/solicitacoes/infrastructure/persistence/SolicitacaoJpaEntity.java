package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence;

import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo;
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao;
import br.ufpr.sept.so2.modules.solicitacoes.domain.SolicitacaoEvento;
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "solicitacao")
public class SolicitacaoJpaEntity extends BaseEntity {

    @Column(name = "tipo_id", nullable = false)
    private UUID tipoId;

    @Column(name = "tipo_codigo", nullable = false, length = 80)
    private String tipoCodigo;

    @Column(name = "tipo_nome", nullable = false, length = 200)
    private String tipoNome;

    @Column(name = "tipo_versao", nullable = false)
    private int tipoVersao;

    @Column(name = "solicitante_id", nullable = false)
    private UUID solicitanteId;

    @Column(nullable = false, unique = true, length = 32)
    private String protocolo;

    @Column(nullable = false, length = 40)
    private String estado;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_schema_snapshot", nullable = false, columnDefinition = "jsonb")
    private String formSchemaSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "workflow_snapshot", nullable = false, columnDefinition = "jsonb")
    private String workflowSnapshot;

    @Column(name = "prazo_em", nullable = false)
    private OffsetDateTime prazoEm;

    @Column(name = "hash_sha256", length = 64)
    private String hashSha256;

    protected SolicitacaoJpaEntity() {
    }

    public static SolicitacaoJpaEntity fromDomain(Solicitacao solicitacao) {
        SolicitacaoJpaEntity entity = new SolicitacaoJpaEntity();
        entity.setId(solicitacao.getId());
        entity.merge(solicitacao);
        return entity;
    }

    public void merge(Solicitacao solicitacao) {
        this.tipoId = solicitacao.getTipoId();
        this.tipoCodigo = solicitacao.getTipoCodigo();
        this.tipoNome = solicitacao.getTipoNome();
        this.tipoVersao = solicitacao.getTipoVersao();
        this.solicitanteId = solicitacao.getSolicitanteId();
        this.protocolo = solicitacao.getProtocolo().getValor();
        this.estado = solicitacao.getEstado();
        this.payload = solicitacao.getPayloadJson();
        this.formSchemaSnapshot = solicitacao.getFormSchemaSnapshot();
        this.workflowSnapshot = solicitacao.getWorkflowSnapshot();
        this.prazoEm = solicitacao.getPrazoEm();
        this.hashSha256 = solicitacao.getHashSha256();
    }

    public Solicitacao toDomain(List<SolicitacaoEvento> eventos) {
        return new Solicitacao(
                getId(),
                tipoId,
                tipoCodigo,
                tipoNome,
                tipoVersao,
                solicitanteId,
                Protocolo.of(protocolo),
                estado,
                payload,
                formSchemaSnapshot,
                workflowSnapshot,
                prazoEm,
                hashSha256,
                eventos,
                getCreatedAt(),
                getUpdatedAt()
        );
    }

    public Solicitacao toDomainSemEventos() {
        return toDomain(List.of());
    }
}
