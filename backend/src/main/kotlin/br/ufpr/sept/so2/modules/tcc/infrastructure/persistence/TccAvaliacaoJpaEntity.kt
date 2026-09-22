package br.ufpr.sept.so2.modules.tcc.infrastructure.persistence

import br.ufpr.sept.so2.modules.tcc.domain.AcaoAvaliacaoTcc
import br.ufpr.sept.so2.modules.tcc.domain.AvaliacaoTcc
import br.ufpr.sept.so2.modules.tcc.domain.PapelBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "tcc_avaliacao")
class TccAvaliacaoJpaEntity : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tcc", nullable = false)
    var tcc: TccJpaEntity? = null

    @Column(name = "id_autor", nullable = false)
    var idAutor: UUID? = null

    @Column(nullable = false, length = 20)
    var papel: String? = null

    @Column(nullable = false, length = 30)
    var acao: String? = null

    @Column(nullable = false, length = 40)
    var resultado: String? = null

    @Column(nullable = false, precision = 3, scale = 1)
    var nota: BigDecimal? = null

    @Column(nullable = false, length = 2000)
    var parecer: String? = null

    fun merge(avaliacao: AvaliacaoTcc) {
        idAutor = avaliacao.idAutor
        papel = avaliacao.papel.name
        acao = avaliacao.acao.name
        resultado = avaliacao.resultado.name
        nota = avaliacao.nota
        parecer = avaliacao.parecer
        if (createdAt == null) {
            createdAt = avaliacao.createdAt
            updatedAt = avaliacao.createdAt
        }
    }

    fun toDomain(): AvaliacaoTcc = AvaliacaoTcc(
        id!!,
        idAutor!!,
        PapelBancaTcc.from(papel),
        AcaoAvaliacaoTcc.from(acao),
        TccEstado.from(resultado),
        nota!!,
        parecer!!,
        createdAt!!,
    )
}
