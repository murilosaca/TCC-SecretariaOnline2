package br.ufpr.sept.so2.modules.certificados.application.ports

import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CertificadoRepository {
    fun save(certificado: Certificado): Certificado

    fun findById(id: UUID): Certificado?

    fun findByIdFormativa(formativaId: UUID): Certificado?

    fun findByHashSha256(hashSha256: String): Certificado?

    fun findByAluno(alunoId: UUID, pageable: Pageable): Page<Certificado>

    fun countByAluno(alunoId: UUID): Long
}
