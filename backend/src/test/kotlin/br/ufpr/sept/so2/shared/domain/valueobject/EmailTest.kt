package br.ufpr.sept.so2.shared.domain.valueobject

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EmailTest : StringSpec({
    "identifica email institucional" {
        Email.of("aluno@ufpr.br").isInstitutional() shouldBe true
        Email.of("aluno@gmail.com").isInstitutional() shouldBe false
    }
})
