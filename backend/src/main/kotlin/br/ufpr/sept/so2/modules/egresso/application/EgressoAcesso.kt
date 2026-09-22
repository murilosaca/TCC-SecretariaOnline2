package br.ufpr.sept.so2.modules.egresso.application

import br.ufpr.sept.so2.modules.egresso.application.ports.EgressoConsultaPort.Cadastro
import br.ufpr.sept.so2.modules.iam.application.CapsSessao
import br.ufpr.sept.so2.shared.domain.exception.AcessoNegadoException

internal object EgressoAcesso {
    fun exigirSemAlunoAtivo(authorities: Collection<String>) {
        if (CapsSessao.alunoAtivo(authorities)) {
            throw AcessoNegadoException("Aluno ativo não acessa o portal do egresso.")
        }
    }

    fun exigirEgresso(cadastro: Cadastro?): Cadastro {
        if (cadastro == null || !cadastro.egresso) {
            throw AcessoNegadoException("Portal do egresso indisponível para esta sessão.")
        }
        return cadastro
    }
}
