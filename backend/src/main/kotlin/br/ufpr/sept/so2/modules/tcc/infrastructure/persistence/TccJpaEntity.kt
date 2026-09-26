package br.ufpr.sept.so2.modules.tcc.infrastructure.persistence

import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import br.ufpr.sept.so2.modules.tcc.domain.TccSituacao
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "tcc")
class TccJpaEntity : BaseEntity() {

    @Column(name = "id_aluno", nullable = false)
    var idAluno: UUID? = null

    @Column(name = "id_curso", nullable = false)
    var idCurso: UUID? = null

    @Column(nullable = false, length = 200)
    var titulo: String? = null

    @Column(nullable = false, length = 20)
    var situacao: String? = null

    @Column(nullable = false, length = 40)
    var estado: String? = null

    @Column(name = "data_defesa", nullable = false)
    var dataDefesa: LocalDate? = null

    @Column(name = "data_entrega", nullable = false)
    var dataEntrega: LocalDate? = null

    @Column(name = "nome_arquivo", length = 255)
    var nomeArquivo: String? = null

    @Column(name = "content_type", length = 100)
    var contentType: String? = null

    /** Legado bytea; nulo após migração para MinIO. */
    @Column(columnDefinition = "bytea")
    var conteudo: ByteArray? = null

    @Column(name = "storage_key", length = 512)
    var storageKey: String? = null

    var tamanho: Int? = null

    @Column(name = "enviado_em")
    var enviadoEm: OffsetDateTime? = null

    @OneToMany(mappedBy = "tcc", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    var membros: MutableList<TccMembroJpaEntity> = mutableListOf()

    @OneToMany(mappedBy = "tcc", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var avaliacoes: MutableList<TccAvaliacaoJpaEntity> = mutableListOf()

    fun merge(tcc: Tcc) {
        idAluno = tcc.idAluno
        idCurso = tcc.idCurso
        titulo = tcc.titulo
        situacao = tcc.situacao.name
        estado = tcc.estado.name
        dataDefesa = tcc.dataDefesa
        dataEntrega = tcc.dataEntrega
        nomeArquivo = tcc.nomeArquivo
        contentType = tcc.contentType
        storageKey = tcc.storageKey
        conteudo = null
        tamanho = tcc.tamanho
        enviadoEm = tcc.enviadoEm
        if (createdAt == null) {
            createdAt = tcc.createdAt
        }
        updatedAt = tcc.updatedAt
        val manter = tcc.membros.map { it.id }.toSet()
        membros.removeIf { it.id !in manter }
        tcc.membros.forEach { membro ->
            val entity = membros.find { it.id == membro.id } ?: TccMembroJpaEntity().also {
                it.id = membro.id
                it.tcc = this
                membros.add(it)
            }
            entity.merge(membro, tcc.createdAt)
        }
        tcc.avaliacoes.forEach { avaliacao ->
            if (avaliacoes.none { it.id == avaliacao.id }) {
                val entity = TccAvaliacaoJpaEntity()
                entity.id = avaliacao.id
                entity.tcc = this
                entity.merge(avaliacao)
                avaliacoes.add(entity)
            }
        }
    }

    fun toDomain(): Tcc = Tcc(
        id!!,
        idAluno!!,
        idCurso!!,
        titulo!!,
        TccSituacao.from(situacao),
        TccEstado.from(estado),
        dataDefesa!!,
        dataEntrega!!,
        nomeArquivo,
        contentType,
        storageKey,
        tamanho,
        enviadoEm,
        createdAt!!,
        updatedAt!!,
        membros.map { it.toDomain() },
        avaliacoes.map { it.toDomain() },
    )

    companion object {
        fun fromDomain(tcc: Tcc): TccJpaEntity {
            val entity = TccJpaEntity()
            entity.id = tcc.id
            entity.merge(tcc)
            return entity
        }
    }
}
