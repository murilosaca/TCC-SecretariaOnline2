package br.ufpr.sept.so2.modules.iam.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PoliticaSenhaTest : StringSpec({
    "aceita senha forte" {
        PoliticaSenha.validar("TroqueEstaSenha1!")
        PoliticaSenha.atende("TroqueEstaSenha1!") shouldBe true
        PoliticaSenha.pontuacao("TroqueEstaSenha1!") shouldBe 4
    }

    "rejeita curta e sem complexidade" {
        shouldThrow<DadoInvalidoException> { PoliticaSenha.validar("curta") }
        shouldThrow<DadoInvalidoException> { PoliticaSenha.validar("semmaiuscula1!") }
        shouldThrow<DadoInvalidoException> { PoliticaSenha.validar("SEMMINUSCULA1!") }
        shouldThrow<DadoInvalidoException> { PoliticaSenha.validar("SemNumero!!aa") }
        shouldThrow<DadoInvalidoException> { PoliticaSenha.validar("SemEspecial12aa") }
        PoliticaSenha.atende("fraca") shouldBe false
        PoliticaSenha.pontuacao("abc") shouldBe 1
    }
})
