package br.ufpr.sept.so2.modules.certificados.infrastructure.persistence

import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import br.ufpr.sept.so2.modules.certificados.domain.CertificadoTipo
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "certificado",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_certificado_formativa", columnNames = ["id_formativa"]),
        UniqueConstraint(name = "uq_certificado_hash", columnNames = ["hash_sha256"]),
    ],
)
class CertificadoJpaEntity : BaseEntity() {

    @Column(name = "id_aluno", nullable = false)
    var idAluno: UUID? = null

    @Column(name = "id_formativa")
    var idFormativa: UUID? = null

    @Column(name = "id_evento")
    var idEvento: UUID? = null

    @Column(nullable = false, length = 40)
    var tipo: String? = null

    @Column(nullable = false, length = 200)
    var titulo: String? = null

    @Column(name = "carga_horaria", nullable = false)
    var cargaHoraria: Int = 0

    @Column(name = "beneficiario_nome", nullable = false, length = 200)
    var beneficiarioNome: String? = null

    @Column(name = "hash_sha256", nullable = false, length = 64)
    var hashSha256: String? = null

    @Column(nullable = false)
    var assinatura: String? = null

    @Column(nullable = false, columnDefinition = "bytea")
    var pdf: ByteArray? = null

    @Column(name = "emitido_em", nullable = false)
    var emitidoEm: OffsetDateTime? = null

    fun merge(certificado: Certificado) {
        idAluno = certificado.idAluno
        idFormativa = certificado.idFormativa
        idEvento = certificado.idEvento
        tipo = certificado.tipo.name
        titulo = certificado.titulo
        cargaHoraria = certificado.cargaHoraria
        beneficiarioNome = certificado.beneficiarioNome
        hashSha256 = certificado.hashSha256
        assinatura = certificado.assinatura
        pdf = certificado.pdf
        emitidoEm = certificado.emitidoEm
    }

    fun toDomain(): Certificado = Certificado(
        id!!,
        idAluno!!,
        idFormativa,
        idEvento,
        CertificadoTipo.from(tipo),
        titulo!!,
        cargaHoraria,
        beneficiarioNome!!,
        hashSha256!!,
        assinatura!!,
        pdf!!,
        emitidoEm!!,
        createdAt!!,
        updatedAt!!,
    )

    companion object {
        fun fromDomain(certificado: Certificado): CertificadoJpaEntity {
            val entity = CertificadoJpaEntity()
            entity.id = certificado.id
            entity.merge(certificado)
            return entity
        }
    }
}
