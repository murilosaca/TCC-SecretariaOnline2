package br.ufpr.sept.so2.modules.suporte.infrastructure.persistence

import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "faq_item")
class FaqItemJpaEntity : BaseEntity() {
    @Column(nullable = false, length = 300)
    var pergunta: String? = null

    @Column(nullable = false, columnDefinition = "text")
    var resposta: String? = null

    @Column(nullable = false)
    var ordem: Int = 0
}
