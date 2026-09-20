package br.ufpr.sept.so2.modules.certificados.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CertificadoJpaRepository : JpaRepository<CertificadoJpaEntity, UUID> {
    fun findByIdFormativa(idFormativa: UUID): CertificadoJpaEntity?

    fun findByHashSha256(hashSha256: String): CertificadoJpaEntity?

    fun findByIdAluno(idAluno: UUID, pageable: Pageable): Page<CertificadoJpaEntity>

    fun countByIdAluno(idAluno: UUID): Long
}
