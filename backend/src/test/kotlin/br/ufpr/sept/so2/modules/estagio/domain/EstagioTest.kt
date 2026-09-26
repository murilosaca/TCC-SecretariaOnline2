package br.ufpr.sept.so2.modules.estagio.domain

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

class EstagioTest : StringSpec({
    val agora = OffsetDateTime.parse("2026-09-21T15:00:00Z")
    val estagioId = UUID.fromString("01800000-0000-7000-8000-000000000101")
    val alunoId = UUID.fromString("01800000-0000-7000-8000-000000000102")
    val cursoId = UUID.fromString("01800000-0000-7000-8000-000000000103")
    val orientadorId = UUID.fromString("01800000-0000-7000-8000-000000000104")
    val tceId = UUID.fromString("01800000-0000-7000-8000-000000000105")
    val relatorioId = UUID.fromString("01800000-0000-7000-8000-000000000106")
    val parecerId = UUID.fromString("01800000-0000-7000-8000-000000000107")
    val pdf = "%PDF-1.4\n".toByteArray(Charsets.US_ASCII)

    fun ativo(): Estagio = Estagio.abrir(
        estagioId,
        alunoId,
        cursoId,
        orientadorId,
        "Empresa Fictícia SEPT",
        "Carla Supervisora",
        LocalDate.parse("2026-03-02"),
        LocalDate.parse("2026-11-30"),
        agora,
        listOf(
            DocumentoEstagio.pendente(tceId, TipoDocumentoEstagio.TCE),
            DocumentoEstagio.pendente(relatorioId, TipoDocumentoEstagio.RELATORIO_FINAL),
        ),
    )

    "estágio ativo nasce com documentos pendentes de envio" {
        val estagio = ativo()
        estagio.situacao shouldBe EstagioSituacao.ATIVO
        estagio.documentos.map { it.estado } shouldBe listOf(
            EstadoDocumentoEstagio.PENDENTE,
            EstadoDocumentoEstagio.PENDENTE,
        )
        estagio.podeArquivar() shouldBe false
        estagio.documentos.all { it.podeEnviar() } shouldBe true
    }

    "estágio sem documento é recusado" {
        shouldThrow<DadoInvalidoException> {
            Estagio.abrir(
                estagioId,
                alunoId,
                cursoId,
                null,
                "Empresa",
                "Supervisor",
                LocalDate.parse("2026-03-02"),
                LocalDate.parse("2026-11-30"),
                agora,
                emptyList(),
            )
        }
    }

    "vigência invertida e tipo repetido são inválidos" {
        shouldThrow<DadoInvalidoException> {
            Estagio.abrir(
                estagioId,
                alunoId,
                cursoId,
                orientadorId,
                "Empresa",
                "Supervisor",
                LocalDate.parse("2026-11-30"),
                LocalDate.parse("2026-03-02"),
                agora,
                listOf(DocumentoEstagio.pendente(tceId, TipoDocumentoEstagio.TCE)),
            )
        }
        shouldThrow<DadoInvalidoException> {
            Estagio.abrir(
                estagioId,
                alunoId,
                cursoId,
                orientadorId,
                "Empresa",
                "Supervisor",
                LocalDate.parse("2026-03-02"),
                LocalDate.parse("2026-11-30"),
                agora,
                listOf(
                    DocumentoEstagio.pendente(tceId, TipoDocumentoEstagio.TCE),
                    DocumentoEstagio.pendente(relatorioId, TipoDocumentoEstagio.TCE),
                ),
            )
        }
    }

    "envio de PDF aguarda parecer e reenvio no mesmo estado conflita" {
        val estagio = ativo()
        estagio.enviarDocumento(TipoDocumentoEstagio.RELATORIO_FINAL, "relatorio.pdf", "application/pdf", pdf, agora)
        val relatorio = estagio.documentos.first { it.tipo == TipoDocumentoEstagio.RELATORIO_FINAL }
        relatorio.estado shouldBe EstadoDocumentoEstagio.AGUARDANDO_PARECER
        relatorio.podeEnviar() shouldBe false
        estagio.documentoPendente() shouldBe "RELATORIO_FINAL"
        shouldThrow<ConflitoEstadoException> {
            estagio.enviarDocumento(TipoDocumentoEstagio.RELATORIO_FINAL, "relatorio.pdf", "application/pdf", pdf, agora)
        }
        shouldThrow<DadoInvalidoException> {
            estagio.enviarDocumento(TipoDocumentoEstagio.TCE, "nota.txt", "text/plain", "oi".toByteArray(), agora)
        }
        estagio.documentos.first { it.tipo == TipoDocumentoEstagio.TCE }.estado shouldBe EstadoDocumentoEstagio.PENDENTE
    }

    "reprovar com parecer curto é inválido e não muda o estado" {
        val estagio = ativo()
        estagio.enviarDocumento(TipoDocumentoEstagio.TCE, "tce.pdf", "application/pdf", pdf, agora)
        shouldThrow<DadoInvalidoException> {
            estagio.emitirParecer(tceId, "REPROVAR", "curto demais", orientadorId, parecerId, agora.plusMinutes(1))
        }
        shouldThrow<DadoInvalidoException> {
            estagio.emitirParecer(tceId, "INDEFERIR", "ainda curto", orientadorId, parecerId, agora.plusMinutes(1))
        }
        estagio.documentos.first { it.id == tceId }.estado shouldBe EstadoDocumentoEstagio.AGUARDANDO_PARECER
        estagio.pareceres() shouldBe emptyList()
    }

    "reprovar libera reenvio e aprovar os obrigatórios permite arquivar" {
        val estagio = ativo()
        estagio.enviarDocumento(TipoDocumentoEstagio.TCE, "tce.pdf", "application/pdf", pdf, agora)
        estagio.emitirParecer(
            tceId,
            "REPROVAR",
            "O TCE está sem a assinatura da concedente.",
            orientadorId,
            parecerId,
            agora.plusMinutes(1),
        )
        estagio.documentos.first { it.id == tceId }.estado shouldBe EstadoDocumentoEstagio.REPROVADO
        estagio.enviarDocumento(TipoDocumentoEstagio.TCE, "tce.pdf", "application/pdf", pdf, agora.plusMinutes(2))
        estagio.documentos.first { it.id == tceId }.estado shouldBe EstadoDocumentoEstagio.AGUARDANDO_PARECER

        estagio.emitirParecer(
            tceId,
            "APROVAR",
            "TCE regular.",
            orientadorId,
            UUID.fromString("01800000-0000-7000-8000-000000000108"),
            agora.plusMinutes(3),
        )
        estagio.enviarDocumento(TipoDocumentoEstagio.RELATORIO_FINAL, "relatorio.pdf", "application/pdf", pdf, agora)
        shouldThrow<DadoInvalidoException> { estagio.encerrar(agora.plusMinutes(4)) }
        estagio.situacao shouldBe EstagioSituacao.ATIVO

        estagio.emitirParecer(
            relatorioId,
            "APROVAR",
            "Relatório final coerente com o plano.",
            orientadorId,
            UUID.fromString("01800000-0000-7000-8000-000000000109"),
            agora.plusMinutes(5),
        )
        estagio.podeArquivar() shouldBe true
        estagio.pareceres().first().acao shouldBe "APROVADO"
        estagio.encerrar(agora.plusMinutes(6))
        estagio.situacao shouldBe EstagioSituacao.CONCLUIDO
        estagio.podeArquivar() shouldBe false
    }

    "estágio concluído é imutável" {
        val estagio = ativo()
        estagio.enviarDocumento(TipoDocumentoEstagio.TCE, "tce.pdf", "application/pdf", pdf, agora)
        estagio.enviarDocumento(TipoDocumentoEstagio.RELATORIO_FINAL, "relatorio.pdf", "application/pdf", pdf, agora)
        estagio.emitirParecer(tceId, "APROVAR", "Ok.", orientadorId, parecerId, agora.plusMinutes(1))
        estagio.emitirParecer(
            relatorioId,
            "APROVAR",
            "Ok.",
            orientadorId,
            UUID.fromString("01800000-0000-7000-8000-00000000010a"),
            agora.plusMinutes(2),
        )
        estagio.encerrar(agora.plusMinutes(3))
        shouldThrow<ConflitoEstadoException> {
            estagio.enviarDocumento(TipoDocumentoEstagio.TCE, "tce.pdf", "application/pdf", pdf, agora.plusMinutes(4))
        }
        shouldThrow<ConflitoEstadoException> {
            estagio.emitirParecer(tceId, "APROVAR", "Novo parecer.", orientadorId, parecerId, agora.plusMinutes(4))
        }
        shouldThrow<ConflitoEstadoException> { estagio.encerrar(agora.plusMinutes(5)) }
        estagio.situacao shouldBe EstagioSituacao.CONCLUIDO
    }

    "estágio sem orientador entra no pool e aceita atribuição" {
        val estagio = Estagio.abrir(
            estagioId,
            alunoId,
            cursoId,
            null,
            "Pool COE SEPT",
            "Ana Supervisora",
            LocalDate.parse("2026-04-01"),
            LocalDate.parse("2026-12-15"),
            agora,
            listOf(DocumentoEstagio.pendente(tceId, TipoDocumentoEstagio.TCE)),
        )
        estagio.semOrientador() shouldBe true
        estagio.podeAtribuir(orientadorId) shouldBe true
        estagio.atribuirOrientador(orientadorId, agora.plusMinutes(1))
        estagio.orientadoPor(orientadorId) shouldBe true
        estagio.semOrientador() shouldBe false
        estagio.podeAtribuir(orientadorId) shouldBe true
        estagio.podeAtribuir(UUID.fromString("01800000-0000-7000-8000-000000000199")) shouldBe false
        shouldThrow<ConflitoEstadoException> {
            estagio.atribuirOrientador(orientadorId, agora.plusMinutes(2))
        }
    }

    "cadastro ativo atualiza vigência e o concluído permanece imutável" {
        val estagio = Estagio.abrir(
            estagioId,
            alunoId,
            cursoId,
            null,
            "Empresa Fictícia SEPT",
            "Carla Supervisora",
            LocalDate.parse("2026-03-02"),
            LocalDate.parse("2026-11-30"),
            agora,
            listOf(
                DocumentoEstagio.pendente(tceId, TipoDocumentoEstagio.TCE),
                DocumentoEstagio.pendente(relatorioId, TipoDocumentoEstagio.RELATORIO_FINAL),
            ),
        )
        estagio.atualizarCadastro(
            "Outra Empresa",
            "Paulo Supervisor",
            LocalDate.parse("2026-04-01"),
            LocalDate.parse("2026-12-15"),
            agora.plusDays(1),
        )
        estagio.empresa shouldBe "Outra Empresa"
        estagio.supervisor shouldBe "Paulo Supervisor"
        estagio.inicio shouldBe LocalDate.parse("2026-04-01")
        estagio.fim shouldBe LocalDate.parse("2026-12-15")
        estagio.semOrientador() shouldBe true
        estagio.situacao shouldBe EstagioSituacao.ATIVO
        estagio.documentos.map { it.tipo } shouldBe listOf(
            TipoDocumentoEstagio.TCE,
            TipoDocumentoEstagio.RELATORIO_FINAL,
        )

        val concluido = ativo()
        concluido.enviarDocumento(TipoDocumentoEstagio.TCE, "tce.pdf", "application/pdf", pdf, agora)
        concluido.enviarDocumento(TipoDocumentoEstagio.RELATORIO_FINAL, "relatorio.pdf", "application/pdf", pdf, agora)
        concluido.emitirParecer(tceId, "APROVAR", "Ok.", orientadorId, parecerId, agora.plusMinutes(1))
        concluido.emitirParecer(
            relatorioId,
            "APROVAR",
            "Ok.",
            orientadorId,
            UUID.fromString("01800000-0000-7000-8000-00000000010b"),
            agora.plusMinutes(2),
        )
        concluido.encerrar(agora.plusMinutes(3))
        shouldThrow<ConflitoEstadoException> {
            concluido.atualizarCadastro(
                "Outra Empresa",
                "Paulo Supervisor",
                LocalDate.parse("2026-04-01"),
                LocalDate.parse("2026-12-15"),
                agora.plusDays(2),
            )
        }
        concluido.empresa shouldBe "Empresa Fictícia SEPT"
        concluido.situacao shouldBe EstagioSituacao.CONCLUIDO
    }
})
