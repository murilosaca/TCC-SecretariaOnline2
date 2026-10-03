package br.ufpr.sept.so2.modules.atendimentos.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.UUID

class AtendimentoTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-10-03T12:00:00Z")
    val id = UUID.fromString("01999999-0000-7000-8000-00000000a025")
    val aluno = UUID.fromString("01999999-0000-7000-8000-00000000a026")
    val categoria = UUID.fromString("01999999-0000-7000-8000-00000000a027")
    val registrador = UUID.fromString("01999999-0000-7000-8000-00000000a028")

    fun registro(): Atendimento = Atendimento.registrar(
        id,
        aluno,
        categoria,
        registrador,
        "Assunto do guichê",
        "Resposta da secretaria",
        null,
        agora,
    )

    "nasce pendente de ciencia sem anexo" {
        val atendimento = registro()
        atendimento.estado shouldBe AtendimentoEstado.PENDENTE_CIENCIA
        atendimento.pendenteDeCiencia() shouldBe true
        atendimento.temAnexo() shouldBe false
        atendimento.cienciaEm shouldBe null
    }

    "ciencia transiciona e segunda tentativa conflita" {
        val atendimento = registro()
        atendimento.darCiencia("127.0.0.1", agora.plusMinutes(5))
        atendimento.estado shouldBe AtendimentoEstado.CIENCIA_DADA
        atendimento.cienciaIp shouldBe "127.0.0.1"
        shouldThrow<ConflitoEstadoException> {
            atendimento.darCiencia("10.0.0.1", agora.plusMinutes(10))
        }.message shouldBe Atendimento.CIENCIA_JA_DADA
    }

    "assunto e resposta sao obrigatorios" {
        shouldThrow<DadoInvalidoException> {
            Atendimento.registrar(id, aluno, categoria, registrador, "  ", "ok", null, agora)
        }.message shouldBe Atendimento.ASSUNTO_OBRIGATORIO
        shouldThrow<DadoInvalidoException> {
            Atendimento.registrar(id, aluno, categoria, registrador, "ok", "   ", null, agora)
        }.message shouldBe Atendimento.RESPOSTA_OBRIGATORIA
    }

    "anexo so aceita PDF de ate 10 MB" {
        shouldThrow<DadoInvalidoException> {
            AnexoAtendimento.validar("nao e pdf".toByteArray())
        }.message shouldBe AnexoAtendimento.MENSAGEM
        shouldThrow<DadoInvalidoException> {
            AnexoAtendimento.validar(ByteArray(AnexoAtendimento.MAX_BYTES + 1) { '%'.code.toByte() })
        }.message shouldBe AnexoAtendimento.MENSAGEM
        AnexoAtendimento.validar("%PDF-1.4 ok".toByteArray())
    }
})
