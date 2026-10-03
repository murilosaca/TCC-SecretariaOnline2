package br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence

import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(name = "template_comunicacao")
class TemplateComunicacaoJpaEntity : BaseEntity() {
    @Column(nullable = false, unique = true, length = 120)
    var nome: String? = null

    @Column(nullable = false, length = 200)
    var assunto: String? = null
}

@Entity
@Table(
    name = "template_comunicacao_revisao",
    uniqueConstraints = [UniqueConstraint(columnNames = ["template_id", "versao"])],
)
class TemplateRevisaoJpaEntity : BaseEntity() {
    @Column(name = "template_id", nullable = false)
    var templateId: UUID? = null

    @Column(nullable = false)
    var versao: Int = 0

    @Column(nullable = false, length = 200)
    var assunto: String? = null

    @Column(nullable = false, columnDefinition = "text")
    var corpo: String? = null

    @Column(nullable = false, length = 16)
    var status: String? = null

    @Column(name = "autor_id", nullable = false)
    var autorId: UUID? = null
}
