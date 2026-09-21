package br.ufpr.sept.so2.modules.estagio.infrastructure.persistence

import br.ufpr.sept.so2.modules.estagio.domain.ParecerEstagio
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "estagio_parecer")
class EstagioParecerJpaEntity : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_documento", nullable = false)
    var documento: EstagioDocumentoJpaEntity? = null

    @Column(name = "id_autor", nullable = false)
    var idAutor: UUID? = null

    @Column(nullable = false, length = 20)
    var acao: String? = null

    @Column(nullable = false, length = 2000)
    var texto: String? = null

    fun merge(parecer: ParecerEstagio) {
        idAutor = parecer.idAutor
        acao = parecer.acao
        texto = parecer.texto
        if (createdAt == null) {
            createdAt = parecer.createdAt
            updatedAt = parecer.createdAt
        }
    }

    fun toDomain(): ParecerEstagio = ParecerEstagio(
        id!!,
        documento?.id!!,
        idAutor!!,
        acao!!,
        texto!!,
        createdAt!!,
    )
}
