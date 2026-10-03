package br.ufpr.sept.so2.modules.atendimentos.infrastructure.persistence

import br.ufpr.sept.so2.modules.atendimentos.domain.Atendimento
import br.ufpr.sept.so2.modules.atendimentos.domain.AtendimentoEstado
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "atendimento")
class AtendimentoJpaEntity : BaseEntity() {

    @Column(name = "id_aluno", nullable = false)
    var idAluno: UUID? = null

    @Column(name = "id_categoria", nullable = false)
    var idCategoria: UUID? = null

    @Column(name = "id_registrador", nullable = false)
    var idRegistrador: UUID? = null

    @Column(nullable = false, length = 200)
    var assunto: String? = null

    @Column(nullable = false, columnDefinition = "text")
    var resposta: String? = null

    @Column(name = "storage_key", length = 512)
    var storageKey: String? = null

    @Column(nullable = false, length = 20)
    var estado: String? = null

    @Column(name = "ciencia_em")
    var cienciaEm: OffsetDateTime? = null

    @Column(name = "ciencia_ip", length = 64)
    var cienciaIp: String? = null

    fun merge(atendimento: Atendimento) {
        idAluno = atendimento.idAluno
        idCategoria = atendimento.idCategoria
        idRegistrador = atendimento.idRegistrador
        assunto = atendimento.assunto
        resposta = atendimento.resposta
        storageKey = atendimento.storageKey
        estado = atendimento.estado.name
        cienciaEm = atendimento.cienciaEm
        cienciaIp = atendimento.cienciaIp
    }

    fun toDomain(): Atendimento = Atendimento(
        id!!,
        idAluno!!,
        idCategoria!!,
        idRegistrador!!,
        assunto!!,
        resposta!!,
        storageKey,
        AtendimentoEstado.from(estado),
        cienciaEm,
        cienciaIp,
        createdAt!!,
        updatedAt!!,
    )

    companion object {
        fun fromDomain(atendimento: Atendimento): AtendimentoJpaEntity {
            val entity = AtendimentoJpaEntity()
            entity.id = atendimento.id
            entity.merge(atendimento)
            return entity
        }
    }
}
