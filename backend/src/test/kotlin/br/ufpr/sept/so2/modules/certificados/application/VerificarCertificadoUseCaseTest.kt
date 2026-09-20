package br.ufpr.sept.so2.modules.certificados.application

import br.ufpr.sept.so2.modules.certificados.application.ports.CertificadoRepository
import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class VerificarCertificadoUseCaseTest : StringSpec({
    "hash inexistente ou malformado vira 404 sem vazar dados" {
        val repo = mockk<CertificadoRepository>()
        val useCase = VerificarCertificadoUseCase(repo)
        shouldThrow<RecursoNaoEncontradoException> { useCase.execute("nao-e-hash") }
        every { repo.findByHashSha256("a".repeat(64)) } returns null
        shouldThrow<RecursoNaoEncontradoException> { useCase.execute("A".repeat(64)) }
    }

    "hash existente devolve o certificado" {
        val repo = mockk<CertificadoRepository>()
        val certificado = mockk<Certificado>()
        every { repo.findByHashSha256("b".repeat(64)) } returns certificado
        VerificarCertificadoUseCase(repo).execute("B".repeat(64)) shouldBe certificado
    }
})
