package br.ufpr.sept.so2.modules.academico.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class VinculoContaAlunoTest : StringSpec({
    val id = UUID.randomUUID()
    val outro = UUID.randomUUID()

    "sem conta existente cria uma nova" {
        VinculoContaAluno.resolver(null, null, "GRR20240001", "a@ufpr.br") shouldBe null
    }

    "mesmo GRR e mesmo e-mail institucional vincula sem duplicar" {
        val conta = ContaExistente(id, "GRR20240001", "a@ufpr.br", true)
        VinculoContaAluno.resolver(conta, conta, "GRR20240001", "a@ufpr.br") shouldBe id
    }

    "GRR de um usuário e e-mail de outro é incompatível" {
        val porGrr = ContaExistente(id, "GRR20240001", "a@ufpr.br", true)
        val porEmail = ContaExistente(outro, "GRR20240002", "b@ufpr.br", true)
        shouldThrow<ConflitoEstadoException> {
            VinculoContaAluno.resolver(porGrr, porEmail, "GRR20240001", "b@ufpr.br")
        }
    }

    "GRR existente com outro e-mail institucional é incompatível" {
        val porGrr = ContaExistente(id, "GRR20240001", "a@ufpr.br", true)
        shouldThrow<ConflitoEstadoException> {
            VinculoContaAluno.resolver(porGrr, null, "GRR20240001", "outro@ufpr.br")
        }
    }

    "usuário inativo não é vinculado" {
        val conta = ContaExistente(id, "GRR20240001", "a@ufpr.br", false)
        shouldThrow<ConflitoEstadoException> {
            VinculoContaAluno.resolver(conta, conta, "GRR20240001", "a@ufpr.br")
        }
    }
})
