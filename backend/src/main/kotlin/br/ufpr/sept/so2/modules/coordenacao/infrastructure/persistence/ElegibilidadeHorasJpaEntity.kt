package br.ufpr.sept.so2.modules.coordenacao.infrastructure.persistence

import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "elegibilidade_horas")
class ElegibilidadeHorasJpaEntity : BaseEntity() {
    @Column(name = "id_curso", nullable = false)
    var idCurso: UUID? = null

    @Column(nullable = false)
    var elegivel: Boolean = false

    @Column(name = "limiar_aplicado", nullable = false)
    var limiarAplicado: Int = 0

    @Column(name = "horas_validadas", nullable = false)
    var horasValidadas: Int = 0

    @Column(name = "congelada_em", nullable = false)
    var congeladaEm: OffsetDateTime? = null
}
