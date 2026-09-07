package br.ufpr.sept.so2.modules.formativas.application

import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort
import br.ufpr.sept.so2.modules.formativas.application.ports.AlunoPorUsuarioPort.AlunoRef
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException
import java.util.UUID

internal object FormativaAcesso {
    fun exigirAlunoAtivo(port: AlunoPorUsuarioPort, usuarioId: UUID): AlunoRef {
        val aluno = port.resolver(usuarioId)
            ?: throw AcessoNegadoException("Cadastro acadêmico de aluno não encontrado.")
        if (aluno.egresso) {
            throw AcessoNegadoException("Egresso não acessa atividades formativas.")
        }
        return aluno
    }

    fun exigirDono(formativa: Formativa, alunoId: UUID) {
        if (formativa.idAluno != alunoId) {
            throw AcessoNegadoException("Você não pode acessar esta formativa.")
        }
    }
}
