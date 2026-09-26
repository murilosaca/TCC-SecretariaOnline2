package br.ufpr.sept.so2.modules.estagio.infrastructure.persistence

import br.ufpr.sept.so2.modules.estagio.domain.DocumentoEstagio
import br.ufpr.sept.so2.modules.estagio.domain.EstadoDocumentoEstagio
import br.ufpr.sept.so2.modules.estagio.domain.TipoDocumentoEstagio
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime

@Entity
@Table(
    name = "estagio_documento",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_estagio_documento_tipo", columnNames = ["id_estagio", "tipo"]),
    ],
)
class EstagioDocumentoJpaEntity : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estagio", nullable = false)
    var estagio: EstagioJpaEntity? = null

    @Column(nullable = false, length = 40)
    var tipo: String? = null

    @Column(nullable = false)
    var obrigatorio: Boolean = true

    @Column(nullable = false, length = 40)
    var estado: String? = null

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

    @OneToMany(mappedBy = "documento", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var pareceres: MutableList<EstagioParecerJpaEntity> = mutableListOf()

    fun merge(documento: DocumentoEstagio) {
        tipo = documento.tipo.name
        obrigatorio = documento.obrigatorio
        estado = documento.estado.name
        nomeArquivo = documento.nomeArquivo
        contentType = documento.contentType
        storageKey = documento.storageKey
        conteudo = null
        tamanho = documento.tamanho
        enviadoEm = documento.enviadoEm
        if (createdAt == null) {
            val agora = documento.enviadoEm ?: OffsetDateTime.now()
            createdAt = agora
            updatedAt = agora
        }
        documento.pareceres.forEach { parecer ->
            if (pareceres.none { it.id == parecer.id }) {
                val entity = EstagioParecerJpaEntity()
                entity.id = parecer.id
                entity.documento = this
                entity.merge(parecer)
                pareceres.add(entity)
            }
        }
    }

    fun toDomain(): DocumentoEstagio = DocumentoEstagio(
        id!!,
        TipoDocumentoEstagio.from(tipo),
        obrigatorio,
        EstadoDocumentoEstagio.from(estado),
        nomeArquivo,
        contentType,
        storageKey,
        tamanho,
        enviadoEm,
        pareceres.map { it.toDomain() },
    )
}
