package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitacao_protocolo_seq")
public class ProtocoloSeqJpaEntity {

    @Id
    private Integer ano;

    @Column(nullable = false)
    private int ultimo;

    protected ProtocoloSeqJpaEntity() {
    }

    public ProtocoloSeqJpaEntity(Integer ano, int ultimo) {
        this.ano = ano;
        this.ultimo = ultimo;
    }

    public Integer getAno() {
        return ano;
    }

    public int getUltimo() {
        return ultimo;
    }

    public void setUltimo(int ultimo) {
        this.ultimo = ultimo;
    }
}
