package br.ufpr.sept.so2.modules.certificados.infrastructure.persistence

import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoRepository
import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CertificadoJpaAdapter(
    private val jpaRepository: CertificadoJpaRepository,
) : CertificadoRepository {

    override fun save(certificado: Certificado): Certificado {
        val entity = jpaRepository.findById(certificado.id).orElseGet { CertificadoJpaEntity.fromDomain(certificado) }
        entity.merge(certificado)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): Certificado? =
        jpaRepository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findByIdFormativa(formativaId: UUID): Certificado? =
        jpaRepository.findByIdFormativa(formativaId)?.toDomain()

    override fun findByHashSha256(hashSha256: String): Certificado? =
        jpaRepository.findByHashSha256(hashSha256)?.toDomain()

    override fun findByAluno(alunoId: UUID, pageable: Pageable): Page<Certificado> =
        jpaRepository.findByIdAluno(alunoId, pageable).map { it.toDomain() }

    override fun countByAluno(alunoId: UUID): Long = jpaRepository.countByIdAluno(alunoId)
}
