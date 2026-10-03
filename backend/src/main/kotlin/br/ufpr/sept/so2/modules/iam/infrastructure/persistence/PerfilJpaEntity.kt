package br.ufpr.sept.so2.modules.iam.infrastructure.persistence

import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "perfil")
class PerfilJpaEntity : BaseEntity() {
    @Column(nullable = false, unique = true, length = 80)
    var nome: String? = null

    @Column(length = 300)
    var descricao: String? = null

    @Column(nullable = false, length = 20)
    var tipo: String? = null

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_authority", joinColumns = [JoinColumn(name = "perfil_id")])
    @Column(name = "authority", nullable = false, length = 80)
    var authorities: MutableSet<String> = HashSet()
}
