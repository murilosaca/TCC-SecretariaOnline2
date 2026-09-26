package br.ufpr.sept.so2.modules.arquivos.infrastructure

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.Duration

class InMemoryObjectStorageAdapterTest : StringSpec({
    val props = StorageProperties(
        mode = "memory",
        endpoint = "http://localhost:9000",
        publicEndpoint = "http://localhost:9000",
        accessKey = "test",
        secretKey = "test",
        bucket = "so2-test",
    )
    val storage = InMemoryObjectStorageAdapter(props)

    beforeEach {
        storage.clear()
    }

    "grava e gera URL pré-assinada GET com TTL" {
        storage.putObject("certificados/a.pdf", "application/pdf", "%PDF".toByteArray())
        storage.exists("certificados/a.pdf") shouldBe true
        val url = storage.presignGetUrl("certificados/a.pdf", Duration.ofMinutes(15), "cert.pdf")
        url shouldContain "so2-test/certificados/a.pdf"
        url shouldContain "X-Amz-Expires=900"
        storage.getObject("certificados/a.pdf")!!.toList() shouldBe "%PDF".toByteArray().toList()
    }

    "presign GET de objeto inexistente falha" {
        shouldThrow<IllegalArgumentException> {
            storage.presignGetUrl("sumiu.pdf", Duration.ofMinutes(15))
        }
    }

    "presign PUT inclui content type" {
        val url = storage.presignPutUrl("tccs/x.pdf", "application/pdf", Duration.ofMinutes(5))
        url shouldContain "tccs/x.pdf"
        url shouldContain "X-Amz-Expires=300"
    }
})
