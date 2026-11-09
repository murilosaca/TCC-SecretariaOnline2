package br.ufpr.sept.so2.modules.solicitacoes.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AnexoPdfTest : StringSpec({
    "pdf dentro do limite devolve o sha-256 e rejeita tipo ou tamanho" {
        val bytes = "%PDF-1.4 anexo".toByteArray()
        val hash = AnexoPdf.validar("pedido.pdf", "application/pdf", bytes, null)
        hash shouldBe AnexoPdf.sha256(bytes)
        shouldThrow<DadoInvalidoException> {
            AnexoPdf.validar("foto.png", "image/png", "nao-pdf".toByteArray(), null)
        }.message shouldBe AnexoPdf.MSG_TIPO
        shouldThrow<DadoInvalidoException> {
            AnexoPdf.validar("grande.pdf", "application/pdf", ByteArray(AnexoPdf.LIMITE_BYTES + 1), null)
        }.message shouldBe AnexoPdf.MSG_TAMANHO
    }
})
