package br.ufpr.sept.so2.modules.certificados.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime
import java.util.UUID

class CertificadoTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-20T18:00:00Z")
    val hash = "a".repeat(64)

    "formativa sem id é inválida" {
        shouldThrow<DadoInvalidoException> {
            Certificado(
                UUID.fromString("01800000-0000-7000-8000-0000000000c1"),
                UUID.fromString("01800000-0000-7000-8000-0000000000a1"),
                null,
                null,
                CertificadoTipo.FORMATIVA,
                "Oficina",
                4,
                "Aluno Dev",
                hash,
                "assinatura",
                byteArrayOf(1, 2, 3),
                agora,
                agora,
                agora,
            )
        }
    }

    "deFormativa guarda tipo e dono" {
        val alunoId = UUID.fromString("01800000-0000-7000-8000-0000000000a1")
        val cert = Certificado.deFormativa(
            UUID.fromString("01800000-0000-7000-8000-0000000000c1"),
            alunoId,
            UUID.fromString("01800000-0000-7000-8000-0000000000f1"),
            UUID.fromString("01800000-0000-7000-8000-0000000000e1"),
            "Oficina",
            4,
            "Aluno Dev",
            hash,
            "assinatura",
            byteArrayOf(1, 2, 3),
            agora,
        )
        cert.tipo shouldBe CertificadoTipo.FORMATIVA
        cert.pertenceAoAluno(alunoId) shouldBe true
        cert.pertenceAoAluno(UUID.fromString("01800000-0000-7000-8000-0000000000a2")) shouldBe false
    }
})
