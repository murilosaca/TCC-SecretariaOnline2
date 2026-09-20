package br.ufpr.sept.so2.shared.api

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import org.springframework.dao.DataIntegrityViolationException

class GlobalExceptionHandlerTest : StringSpec({
    "resumo de integridade no WARN nao inclui GRR" {
        val ex = DataIntegrityViolationException(
            "could not execute statement",
            RuntimeException(
                "ERROR: duplicate key value violates unique constraint \"aluno_grr_key\" " +
                    "Detail: Key (grr)=(GRR20240001) already exists.",
            ),
        )
        val resumo = GlobalExceptionHandler.resumoViolacaoIntegridade(ex)
        resumo.shouldNotContain("GRR20240001")
        resumo.shouldBe("RuntimeException constraint=aluno_grr_key")
    }
})
