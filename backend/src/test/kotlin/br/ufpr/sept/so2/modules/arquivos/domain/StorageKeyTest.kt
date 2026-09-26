package br.ufpr.sept.so2.modules.arquivos.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class StorageKeyTest : StringSpec({
    "monta chaves canônicas" {
        val id = UUID.fromString("01800000-0000-7000-8000-000000000001")
        StorageKey.certificado(id).value shouldBe "certificados/$id.pdf"
        StorageKey.tccVersaoFinal(id).value shouldBe "tccs/$id/versao-final.pdf"
        StorageKey.estagioDocumento(id, id).value shouldBe "estagios/$id/$id.pdf"
    }

    "rejeita chave inválida" {
        shouldThrow<IllegalArgumentException> { StorageKey("") }
        shouldThrow<IllegalArgumentException> { StorageKey("/abs") }
        shouldThrow<IllegalArgumentException> { StorageKey("a/../b") }
    }
})
