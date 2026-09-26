package br.ufpr.sept.so2.modules.tcc.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class TccTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-21T15:00:00Z")
    val tccId = UUID.fromString("01800000-0000-7000-8000-000000000201")
    val alunoId = UUID.fromString("01800000-0000-7000-8000-000000000202")
    val cursoId = UUID.fromString("01800000-0000-7000-8000-000000000203")
    val orientadorId = UUID.fromString("01800000-0000-7000-8000-000000000204")
    val membroId = UUID.fromString("01800000-0000-7000-8000-000000000205")
    val avaliacaoId = UUID.fromString("01800000-0000-7000-8000-000000000206")
    val pdf = "%PDF-1.4\n".toByteArray(Charsets.US_ASCII)
    val parecerLongo = "Parecer individual com fundamentação suficiente."

    fun ativo(): Tcc = Tcc.abrir(
        tccId,
        alunoId,
        cursoId,
        "Plataforma de secretaria acadêmica para o SEPT",
        LocalDate.parse("2026-11-12"),
        LocalDate.parse("2026-10-03"),
        agora,
        listOf(MembroBancaTcc(membroId, orientadorId, PapelBancaTcc.ORIENTADOR)),
    )

    "tcc ativo nasce em elaboração com orientador e sem arquivo" {
        val tcc = ativo()
        tcc.situacao shouldBe TccSituacao.ATIVO
        tcc.estado shouldBe TccEstado.EM_ELABORACAO
        tcc.podeEnviar() shouldBe true
        tcc.podeAvaliar(orientadorId) shouldBe false
        tcc.temArquivo() shouldBe false
        tcc.idOrientador() shouldBe orientadorId
    }

    "defesa anterior à entrega e banca inválida são rejeitadas" {
        shouldThrow<DadoInvalidoException> {
            Tcc.abrir(
                tccId,
                alunoId,
                cursoId,
                "Título válido",
                LocalDate.parse("2026-10-01"),
                LocalDate.parse("2026-10-03"),
                agora,
                listOf(MembroBancaTcc(membroId, orientadorId, PapelBancaTcc.ORIENTADOR)),
            )
        }
        shouldThrow<DadoInvalidoException> {
            Tcc.abrir(
                tccId,
                alunoId,
                cursoId,
                "Título válido",
                LocalDate.parse("2026-11-12"),
                LocalDate.parse("2026-10-03"),
                agora,
                listOf(
                    MembroBancaTcc(membroId, orientadorId, PapelBancaTcc.ORIENTADOR),
                    MembroBancaTcc(UUID.fromString("01800000-0000-7000-8000-000000000207"), orientadorId, PapelBancaTcc.BANCA),
                ),
            )
        }
    }

    "envio de PDF muda para submetido e arquivo inválido não altera o estado" {
        val tcc = ativo()
        shouldThrow<DadoInvalidoException> {
            tcc.enviarVersaoFinal("nota.txt", "text/plain", "nao e pdf".toByteArray(), agora)
        }
        tcc.estado shouldBe TccEstado.EM_ELABORACAO

        tcc.enviarVersaoFinal("tcc.pdf", "application/pdf", pdf, agora)
        tcc.estado shouldBe TccEstado.SUBMETIDO
        tcc.podeEnviar() shouldBe false
        tcc.podeAvaliar(orientadorId) shouldBe true

        shouldThrow<ConflitoEstadoException> {
            tcc.enviarVersaoFinal("tcc.pdf", "application/pdf", pdf, agora)
        }
        tcc.estado shouldBe TccEstado.SUBMETIDO
    }

    "indeferir curto não muda o estado e o longo reprova" {
        val tcc = ativo()
        tcc.enviarVersaoFinal("tcc.pdf", "application/pdf", pdf, agora)
        shouldThrow<DadoInvalidoException> {
            tcc.avaliar("INDEFERIR", BigDecimal("4"), "curto", orientadorId, avaliacaoId, agora)
        }
        tcc.estado shouldBe TccEstado.SUBMETIDO
        tcc.avaliacoes.size shouldBe 0

        tcc.avaliar("REPROVAR", BigDecimal("4.0"), parecerLongo, orientadorId, avaliacaoId, agora)
        tcc.estado shouldBe TccEstado.REPROVADO
        tcc.avaliacoes.single().acao shouldBe AcaoAvaliacaoTcc.INDEFERIR
        shouldThrow<ConflitoEstadoException> {
            tcc.avaliar("APROVAR", BigDecimal("8.5"), "Tarde demais.", orientadorId, UUID.randomUUID(), agora)
        }
    }

    "correções devolvem o envio e a aprovação seguinte encerra a avaliação" {
        val tcc = ativo()
        tcc.enviarVersaoFinal("tcc.pdf", "application/pdf", pdf, agora)
        tcc.avaliar("SOLICITAR_CORRECOES", BigDecimal("6.5"), parecerLongo, orientadorId, avaliacaoId, agora)
        tcc.estado shouldBe TccEstado.CORRECOES_SOLICITADAS
        tcc.podeEnviar() shouldBe true
        tcc.podeAvaliar(orientadorId) shouldBe false

        tcc.enviarVersaoFinal("tcc-v2.pdf", "application/pdf", pdf, agora.plusMinutes(1))
        tcc.estado shouldBe TccEstado.SUBMETIDO
        shouldThrow<DadoInvalidoException> {
            tcc.avaliar("APROVAR", BigDecimal("10.55"), "Nota inválida neste parecer.", orientadorId, UUID.randomUUID(), agora)
        }
        tcc.avaliar("APROVAR", BigDecimal("8.5"), "Versão final adequada.", orientadorId, UUID.randomUUID(), agora)
        tcc.estado shouldBe TccEstado.APROVADO
        tcc.avaliacoes.size shouldBe 2
        tcc.podeEnviar() shouldBe false
    }

    "atualizar cadastro troca título, datas e banca em ativo" {
        val tcc = ativo()
        val outroOrientador = UUID.fromString("01800000-0000-7000-8000-000000000208")
        val bancaId = UUID.fromString("01800000-0000-7000-8000-000000000209")
        tcc.atualizarCadastro(
            "  Título atualizado do TCC  ",
            LocalDate.parse("2026-12-01"),
            LocalDate.parse("2026-11-01"),
            listOf(
                MembroBancaTcc(membroId, outroOrientador, PapelBancaTcc.ORIENTADOR),
                MembroBancaTcc(bancaId, orientadorId, PapelBancaTcc.BANCA),
            ),
            agora.plusMinutes(5),
        )
        tcc.titulo shouldBe "Título atualizado do TCC"
        tcc.dataDefesa shouldBe LocalDate.parse("2026-12-01")
        tcc.idOrientador() shouldBe outroOrientador
        tcc.membros.size shouldBe 2
    }

    "concluído não aceita atualização de cadastro" {
        val tcc = Tcc(
            tccId,
            alunoId,
            cursoId,
            "Título",
            TccSituacao.CONCLUIDO,
            TccEstado.APROVADO,
            LocalDate.parse("2026-11-12"),
            LocalDate.parse("2026-10-03"),
            null,
            null,
            null,
            null,
            null,
            agora,
            agora,
            listOf(MembroBancaTcc(membroId, orientadorId, PapelBancaTcc.ORIENTADOR)),
        )
        shouldThrow<ConflitoEstadoException> {
            tcc.atualizarCadastro(
                "Outro",
                LocalDate.parse("2026-11-12"),
                LocalDate.parse("2026-10-03"),
                listOf(MembroBancaTcc(membroId, orientadorId, PapelBancaTcc.ORIENTADOR)),
                agora,
            )
        }
    }
})
