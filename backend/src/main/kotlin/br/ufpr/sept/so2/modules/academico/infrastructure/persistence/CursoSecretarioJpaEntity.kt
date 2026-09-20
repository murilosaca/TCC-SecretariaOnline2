package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "curso_secretario")
@IdClass(CursoSecretarioId::class)
class CursoSecretarioJpaEntity {
    @Id
    @Column(name = "id_curso", nullable = false)
    var idCurso: UUID? = null

    @Id
    @Column(name = "id_usuario", nullable = false)
    var idUsuario: UUID? = null
}
