package br.ufpr.sept.so2.shared.domain.valueobject

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GrrTest : StringSpec({
    "aceita GRR válido" {
        Grr.of("grr20241234").value shouldBe "GRR20241234"
    }

    "rejeita formato inválido" {
        shouldThrow<IllegalArgumentException> { Grr.of("20241234") }
    }
})
