package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "solicitacao_protocolo_seq")
class ProtocoloSeqJpaEntity {

    @field:Id
    var ano: Int? = null

    @field:Column(nullable = false)
    var ultimo: Int = 0

    protected constructor()

    constructor(ano: Int?, ultimo: Int) {
        this.ano = ano
        this.ultimo = ultimo
    }
}
