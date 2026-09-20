package br.ufpr.sept.so2.modules.certificados.application

import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoRepository
import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VerificarCertificadoUseCase(
    private val certificadoRepository: CertificadoRepository,
) {
    @Transactional(readOnly = true)
    fun execute(hash: String): Certificado {
        val normalizado = hash.trim().lowercase()
        if (!Certificado.HASH_SHA256.matches(normalizado)) {
            throw RecursoNaoEncontradoException("Certificado não encontrado.")
        }
        return certificadoRepository.findByHashSha256(normalizado)
            ?: throw RecursoNaoEncontradoException("Certificado não encontrado.")
    }
}
