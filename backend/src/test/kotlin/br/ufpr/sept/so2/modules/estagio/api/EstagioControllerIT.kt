package br.ufpr.sept.so2.modules.estagio.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.DocumentoEstagio
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.TipoDocumentoEstagio
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstagioControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var cursoRepository: CursoRepository

    @Autowired
    private lateinit var alunoRepository: AlunoRepository

    @Autowired
    private lateinit var estagioRepository: EstagioRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoId: UUID
    private lateinit var alunoId: UUID
    private lateinit var orientadorId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        cursoId = garantirCurso(agora)
        usuariosIt.criarUsuario(EMAIL_ALUNO, GRR_ALUNO, listOf("dashboard.view_own", "internship.view_own"))
        alunoId = criarAluno("Aluno Estágio IT", GRR_ALUNO, EMAIL_ALUNO, cursoId, agora)
        usuariosIt.criarUsuario(EMAIL_OUTRO, GRR_OUTRO, listOf("dashboard.view_own", "internship.view_own"))
        criarAluno("Outro Aluno Estágio", GRR_OUTRO, EMAIL_OUTRO, cursoId, agora)
        orientadorId = usuariosIt.criarUsuario(
            EMAIL_PROFESSOR,
            GRR_PROFESSOR,
            listOf("dashboard.view_own", "internship.review"),
        ).id
        usuariosIt.criarUsuario(EMAIL_ALHEIO, GRR_ALHEIO, listOf("dashboard.view_own", "internship.review"))
        usuariosIt.criarUsuario(EMAIL_SEMCAP, GRR_SEMCAP, listOf("dashboard.view_own"))
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/estagios").param("aluno", "me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityRecebe403NaListaENaFila() {
        val tokenSemCap = usuariosIt.login(EMAIL_SEMCAP)
        mockMvc.perform(
            get("/estagios").param("aluno", "me").header("Authorization", "Bearer $tokenSemCap"),
        )
            .andExpect(status().isForbidden)

        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(
            get("/estagios").param("canReview", "true").header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isForbidden)

        val tokenProfessor = usuariosIt.login(EMAIL_PROFESSOR)
        mockMvc.perform(
            get("/estagios").param("aluno", "me").header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun outroAlunoEOrientadorAlheioRecebem404() {
        val estagio = abrirEstagio()
        val tokenOutro = usuariosIt.login(EMAIL_OUTRO)
        mockMvc.perform(
            get("/estagios/${estagio.id}").header("Authorization", "Bearer $tokenOutro"),
        )
            .andExpect(status().isNotFound)

        val tokenAlheio = usuariosIt.login(EMAIL_ALHEIO)
        mockMvc.perform(
            get("/estagios/${estagio.id}").header("Authorization", "Bearer $tokenAlheio"),
        )
            .andExpect(status().isNotFound)
        mockMvc.perform(
            get("/estagios").param("canReview", "true").header("Authorization", "Bearer $tokenAlheio"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${estagio.id}')]").doesNotExist())
    }

    @Test
    fun alunoEnviaPdfOrientadorEmiteParecerEArquiva() {
        val estagio = abrirEstagio()
        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)
        val antesEnvio = contarOutbox("estagio.documento_enviado")

        mockMvc.perform(
            get("/estagios").param("aluno", "me").header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.novo").doesNotExist())
            .andExpect(jsonPath("$.content[0].empresa").value(EMPRESA))
            .andExpect(jsonPath("$.content[0]._links.novo").doesNotExist())

        val detalhe = ler(
            mockMvc.perform(
                get("/estagios/${estagio.id}").header("Authorization", "Bearer $tokenAluno"),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.documentos[?(@.tipo=='RELATORIO_FINAL')]._links.upload").exists())
                .andExpect(jsonPath("$.documentos[?(@.tipo=='TCE')]._links.aprovar").doesNotExist())
                .andExpect(jsonPath("$._links.arquivar").doesNotExist())
                .andReturn()
                .response
                .contentAsString,
        )

        val relatorioId = documentoId(detalhe, "RELATORIO_FINAL")
        val tceId = documentoId(detalhe, "TCE")

        mockMvc.perform(
            multipart("/estagios/${estagio.id}/documentos")
                .file(pdf("nota.txt", "text/plain", "nao e pdf".toByteArray()))
                .param("tipo", "RELATORIO_FINAL")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isUnprocessableEntity)

        mockMvc.perform(
            multipart("/estagios/${estagio.id}/documentos")
                .file(pdf("relatorio.pdf", MediaType.APPLICATION_PDF_VALUE, PDF))
                .param("tipo", "RELATORIO_FINAL")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.documentos[?(@.tipo=='RELATORIO_FINAL')].estado").value("AGUARDANDO_PARECER"))
            .andExpect(jsonPath("$.documentos[?(@.tipo=='RELATORIO_FINAL')]._links.upload").doesNotExist())
            .andExpect(jsonPath("$.pareceres").isEmpty)

        org.junit.jupiter.api.Assertions.assertEquals(antesEnvio + 1, contarOutbox("estagio.documento_enviado"))

        mockMvc.perform(
            multipart("/estagios/${estagio.id}/documentos")
                .file(pdf("relatorio.pdf", MediaType.APPLICATION_PDF_VALUE, PDF))
                .param("tipo", "RELATORIO_FINAL")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isConflict)

        val tokenProfessor = usuariosIt.login(EMAIL_PROFESSOR)
        mockMvc.perform(
            get("/estagios").param("canReview", "true").header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${estagio.id}')].documentoPendente").value("RELATORIO_FINAL"))
            .andExpect(jsonPath("$.content[?(@.id=='${estagio.id}')]._links.revisar").exists())

        mockMvc.perform(
            post("/estagios/${estagio.id}/documentos/$relatorioId/parecer")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"acao":"REPROVAR","parecer":"curto"}"""),
        )
            .andExpect(status().isUnprocessableEntity)

        mockMvc.perform(
            post("/estagios/${estagio.id}/encerrar")
                .header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isUnprocessableEntity)

        mockMvc.perform(
            post("/estagios/${estagio.id}/encerrar")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isForbidden)

        val antesParecer = contarOutbox("estagio.parecer_emitido")
        mockMvc.perform(
            multipart("/estagios/${estagio.id}/documentos")
                .file(pdf("tce.pdf", MediaType.APPLICATION_PDF_VALUE, PDF))
                .param("tipo", "TCE")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)

        mockMvc.perform(
            post("/estagios/${estagio.id}/documentos/$tceId/parecer")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"acao":"APROVAR","parecer":"TCE regular."}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.arquivar").doesNotExist())

        mockMvc.perform(
            post("/estagios/${estagio.id}/documentos/$relatorioId/parecer")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"acao":"APROVAR","parecer":"Relatório coerente com o plano de estágio."}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.arquivar").exists())
            .andExpect(jsonPath("$.pareceres[0].acao").value("APROVADO"))

        org.junit.jupiter.api.Assertions.assertTrue(contarOutbox("estagio.parecer_emitido") >= antesParecer + 2)

        val antesEncerrar = contarOutbox("estagio.encerrado")
        mockMvc.perform(
            post("/estagios/${estagio.id}/encerrar")
                .header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.situacao").value("CONCLUIDO"))
            .andExpect(jsonPath("$._links.arquivar").doesNotExist())

        org.junit.jupiter.api.Assertions.assertEquals(antesEncerrar + 1, contarOutbox("estagio.encerrado"))

        mockMvc.perform(
            post("/estagios/${estagio.id}/documentos/$relatorioId/parecer")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"acao":"APROVAR","parecer":"Não deve aceitar depois de concluído."}"""),
        )
            .andExpect(status().isConflict)

        mockMvc.perform(
            get("/estagios/${estagio.id}").header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.pareceres[0].texto").exists())
            .andExpect(jsonPath("$.documentos[?(@.tipo=='RELATORIO_FINAL')]._links.upload").doesNotExist())
    }

    private fun abrirEstagio(): Estagio {
        val agora = OffsetDateTime.now()
        return estagioRepository.save(
            Estagio.abrir(
                Uuids.v7(),
                alunoId,
                cursoId,
                orientadorId,
                EMPRESA,
                "Carla Supervisora",
                LocalDate.of(2026, 3, 2),
                LocalDate.of(2026, 11, 30),
                agora,
                listOf(
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.TCE),
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.RELATORIO_FINAL),
                ),
            ),
        )
    }

    private fun garantirCurso(agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(CODIGO)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(
            Curso(Uuids.v7(), "TADS IT Estágio", "TADEST", CODIGO, null, 120, true, agora, agora),
        ).id
    }

    private fun criarAluno(
        nome: String,
        grr: String,
        email: String,
        idCurso: UUID,
        agora: OffsetDateTime,
    ): UUID {
        val existente = alunoRepository.findByGrr(grr)
        if (existente.isPresent) {
            return existente.get().id
        }
        return alunoRepository.save(
            Aluno(
                Uuids.v7(),
                nome,
                null,
                Grr.of(grr),
                Email.of(email),
                null,
                null,
                idCurso,
                AlunoSituacao.MATRICULADO,
                true,
                agora,
                agora,
            ),
        ).id
    }

    private fun contarOutbox(tipo: String): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from outbox_event where tipo = ?",
            Int::class.java,
            tipo,
        ) ?: 0

    private fun ler(json: String): JsonNode = objectMapper.readTree(json)

    private fun documentoId(json: JsonNode, tipo: String): String =
        json.get("documentos").first { it.get("tipo").asText() == tipo }.get("id").asText()

    companion object {
        private const val EMAIL_ALUNO = "it.estagio.aluno@ufpr.br"
        private const val EMAIL_OUTRO = "it.estagio.outro@ufpr.br"
        private const val EMAIL_PROFESSOR = "it.estagio.prof@ufpr.br"
        private const val EMAIL_ALHEIO = "it.estagio.alheio@ufpr.br"
        private const val EMAIL_SEMCAP = "it.estagio.semcap@ufpr.br"
        private const val GRR_ALUNO = "GRR20249101"
        private const val GRR_OUTRO = "GRR20249102"
        private const val GRR_PROFESSOR = "GRR20249103"
        private const val GRR_ALHEIO = "GRR20249104"
        private const val GRR_SEMCAP = "GRR20249105"
        private const val CODIGO = "TADS-EST-IT"
        private const val EMPRESA = "Empresa Fictícia IT"
        private val PDF = "%PDF-1.4\n".toByteArray(Charsets.US_ASCII)

        private fun pdf(nome: String, contentType: String, bytes: ByteArray): MockMultipartFile =
            MockMultipartFile("arquivo", nome, contentType, bytes)
    }
}
