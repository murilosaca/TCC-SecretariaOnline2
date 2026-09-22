package br.ufpr.sept.so2.modules.coordenacao.infrastructure.persistence

import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "curso_configuracao")
class CursoConfiguracaoJpaEntity : BaseEntity() {
    @Column(name = "duracao_calendario", nullable = false)
    var duracaoCalendario: Int = 15

    @Column(name = "banca_membros_externos", nullable = false)
    var bancaMembrosExternos: Int = 1

    @Column(name = "banca_modalidade", nullable = false, length = 20)
    var bancaModalidade: String = "PRESENCIAL"

    @Column(nullable = false, length = 10000)
    var regimento: String = ""
}
