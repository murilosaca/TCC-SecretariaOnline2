package br.ufpr.sept.so2.modules.iam.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class IdentificadorLoginTest : StringSpec({
    "normaliza e-mail e GRR" {
        val email = IdentificadorLogin.tryParse("Aluno.Dev@UFPR.BR")!!
        email.tipo shouldBe IdentificadorLogin.Tipo.EMAIL
        email.valor shouldBe "aluno.dev@ufpr.br"

        val grr = IdentificadorLogin.tryParse("grr20240001")!!
        grr.tipo shouldBe IdentificadorLogin.Tipo.GRR
        grr.valor shouldBe "GRR20240001"

        val soDigitos = IdentificadorLogin.tryParse("20240001")!!
        soDigitos.valor shouldBe "GRR20240001"
    }

    "mascara identificador e rejeita lixo" {
        IdentificadorLogin.tryParse("aluno.dev@ufpr.br")!!.mascarado() shouldBe "a***@ufpr.br"
        IdentificadorLogin.tryParse("GRR20240001")!!.mascarado() shouldBe "GRR****0001"
        IdentificadorLogin.tryParse("nao-e-identificador") shouldBe null
        IdentificadorLogin.tryParse("") shouldBe null
    }
})
