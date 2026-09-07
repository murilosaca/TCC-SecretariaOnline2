package br.ufpr.sept.so2.modules.academico.infrastructure.persistence

import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "periodo_letivo")
class PeriodoLetivoJpaEntity : BaseEntity() {
    @Column(nullable = false)
    var ano: Short = 0

    @Column(nullable = false)
    var semestre: Short = 0

    @Column(nullable = false)
    var inicio: LocalDate? = null

    @Column(nullable = false)
    var fim: LocalDate? = null

    @Column(nullable = false)
    var ativo: Boolean = false

    fun merge(periodo: PeriodoLetivo) {
        ano = periodo.ano.toShort()
        semestre = periodo.semestre.toShort()
        inicio = periodo.inicio
        fim = periodo.fim
        ativo = periodo.ativo
    }

    fun toDomain() = PeriodoLetivo(
        requireNotNull(id),
        ano.toInt(),
        semestre.toInt(),
        requireNotNull(inicio),
        requireNotNull(fim),
        ativo,
        requireNotNull(createdAt),
        requireNotNull(updatedAt),
    )

    companion object {
        fun fromDomain(periodo: PeriodoLetivo) = PeriodoLetivoJpaEntity().apply {
            id = periodo.id
            merge(periodo)
        }
    }
}
