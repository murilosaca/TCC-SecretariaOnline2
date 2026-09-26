package br.ufpr.sept.so2.modules.formativas.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "comissao_membro")
@IdClass(ComissaoMembroId::class)
class ComissaoMembroJpaEntity {
    @Id
    @Column(name = "id_curso", nullable = false)
    var idCurso: UUID? = null

    @Id
    @Column(name = "id_usuario", nullable = false)
    var idUsuario: UUID? = null

    @Id
    @Column(nullable = false, length = 16)
    var tipo: String? = null
}
