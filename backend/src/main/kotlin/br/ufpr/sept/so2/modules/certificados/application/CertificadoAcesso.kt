package br.ufpr.sept.so2.modules.certificados.application

import br.ufpr.sept.so2.modules.certificados.application.ports.BeneficiarioPort
import br.ufpr.sept.so2.modules.certificados.application.ports.BeneficiarioPort.BeneficiarioRef
import br.ufpr.sept.so2.modules.certificados.domain.Certificado
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException
import java.util.UUID

internal object CertificadoAcesso {
    fun exigirAlunoAtivo(port: BeneficiarioPort, usuarioId: UUID): BeneficiarioRef {
        val aluno = port.resolverPorUsuario(usuarioId)
            ?: throw AcessoNegadoException("Cadastro acadêmico de aluno não encontrado.")
        if (aluno.egresso) {
            throw AcessoNegadoException("Egresso não acessa certificados de aluno nesta fatia.")
        }
        return aluno
    }

    fun exigirDono(certificado: Certificado, alunoId: UUID) {
        if (!certificado.pertenceAoAluno(alunoId)) {
            throw RecursoNaoEncontradoException("Certificado não encontrado.")
        }
    }
}
