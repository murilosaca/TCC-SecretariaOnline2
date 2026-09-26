package br.ufpr.sept.so2.modules.tcc.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.MembroBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.PapelBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
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
class TccControllerIT {

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
    private lateinit var tccRepository: TccRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoId: UUID
    private lateinit var alunoId: UUID
    private lateinit var orientadorId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        cursoId = garantirCurso(agora)
        usuariosIt.criarUsuario(EMAIL_ALUNO, GRR_ALUNO, listOf("dashboard.view_own", "tcc.view_own"))
        alunoId = criarAluno("Aluno TCC IT", GRR_ALUNO, EMAIL_ALUNO, cursoId, agora)
        usuariosIt.criarUsuario(EMAIL_OUTRO, GRR_OUTRO, listOf("dashboard.view_own", "tcc.view_own"))
        criarAluno("Outro Aluno TCC", GRR_OUTRO, EMAIL_OUTRO, cursoId, agora)
        orientadorId = usuariosIt.criarUsuario(
            EMAIL_PROFESSOR,
            GRR_PROFESSOR,
            listOf("dashboard.view_own", "tcc.review"),
        ).id
        usuariosIt.criarUsuario(EMAIL_ALHEIO, GRR_ALHEIO, listOf("dashboard.view_own", "tcc.review"))
        usuariosIt.criarUsuario(EMAIL_SEMCAP, GRR_SEMCAP, listOf("dashboard.view_own"))
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/tccs").param("aluno", "me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityRecebe403NaListaENaFila() {
        val tokenSemCap = usuariosIt.login(EMAIL_SEMCAP)
        mockMvc.perform(
            get("/tccs").param("aluno", "me").header("Authorization", "Bearer $tokenSemCap"),
        )
            .andExpect(status().isForbidden)

        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(
            get("/tccs").param("canReview", "true").header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isForbidden)

        val tokenProfessor = usuariosIt.login(EMAIL_PROFESSOR)
        mockMvc.perform(
            get("/tccs").param("aluno", "me").header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun outroAlunoERevisorAlheioRecebem404() {
        val tcc = abrirTcc()
        val tokenOutro = usuariosIt.login(EMAIL_OUTRO)
        mockMvc.perform(
            get("/tccs/${tcc.id}").header("Authorization", "Bearer $tokenOutro"),
        )
            .andExpect(status().isNotFound)

        val tokenAlheio = usuariosIt.login(EMAIL_ALHEIO)
        mockMvc.perform(
            get("/tccs/${tcc.id}").header("Authorization", "Bearer $tokenAlheio"),
        )
            .andExpect(status().isNotFound)
        mockMvc.perform(
            get("/tccs").param("canReview", "true").header("Authorization", "Bearer $tokenAlheio"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${tcc.id}')]").doesNotExist())
    }

    @Test
    fun alunoEnviaPdfEOrientadorAvaliaSemEmitirCertificado() {
        val tcc = abrirTcc()
        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)
        val tokenProfessor = usuariosIt.login(EMAIL_PROFESSOR)
        val antesEnvio = contar("outbox_event", "tcc.submitted")
        val certificadosAntes = contarTabela("certificado")

        mockMvc.perform(
            get("/tccs").param("aluno", "me").header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.novo").doesNotExist())
            .andExpect(jsonPath("$.content[0].titulo").value(TITULO))
            .andExpect(jsonPath("$.content[0].situacao").value("ATIVO"))
            .andExpect(jsonPath("$.content[0].estado").value("EM_ELABORACAO"))
            .andExpect(jsonPath("$.content[0]._links.novo").doesNotExist())
            .andExpect(jsonPath("$.content[0]._links.upload-final").exists())

        mockMvc.perform(
            get("/tccs").param("canReview", "true").header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${tcc.id}')]").doesNotExist())

        mockMvc.perform(
            multipart("/tccs/${tcc.id}/versao-final")
                .file(pdf("nota.txt", "text/plain", "nao e pdf".toByteArray()))
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isUnprocessableEntity)

        mockMvc.perform(
            multipart("/tccs/${tcc.id}/versao-final")
                .file(pdf("tcc.pdf", MediaType.APPLICATION_PDF_VALUE, PDF))
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("SUBMETIDO"))
            .andExpect(jsonPath("$._links.upload-final").doesNotExist())
            .andExpect(jsonPath("$._links.download").exists())
            .andExpect(jsonPath("$._links.avaliar").doesNotExist())

        org.junit.jupiter.api.Assertions.assertEquals(antesEnvio + 1, contar("outbox_event", "tcc.submitted"))

        mockMvc.perform(
            multipart("/tccs/${tcc.id}/versao-final")
                .file(pdf("tcc.pdf", MediaType.APPLICATION_PDF_VALUE, PDF))
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isConflict)

        mockMvc.perform(
            get("/tccs/${tcc.id}/arquivo").header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.downloadUrl").isString)
            .andExpect(jsonPath("$.expiresInSeconds").value(900))

        mockMvc.perform(
            post("/tccs/${tcc.id}/avaliacoes")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"acao":"INDEFERIR","nota":3.0,"parecer":"curto"}"""),
        )
            .andExpect(status().isUnprocessableEntity)

        val antesParecer = contar("outbox_event", "tcc.reviewed")
        mockMvc.perform(
            post("/tccs/${tcc.id}/avaliacoes")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"acao":"APROVAR","nota":8.5,"parecer":"Versão final adequada."}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("APROVADO"))
            .andExpect(jsonPath("$._links.avaliar").doesNotExist())
            .andExpect(jsonPath("$.avaliacoes[0].nota").value(8.5))
            .andExpect(jsonPath("$.papel").value("ORIENTADOR"))

        org.junit.jupiter.api.Assertions.assertEquals(antesParecer + 1, contar("outbox_event", "tcc.reviewed"))
        org.junit.jupiter.api.Assertions.assertEquals(certificadosAntes, contarTabela("certificado"))

        mockMvc.perform(
            post("/tccs/${tcc.id}/avaliacoes")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"acao":"APROVAR","nota":9.0,"parecer":"Segunda avaliação não cabe."}"""),
        )
            .andExpect(status().isConflict)

        mockMvc.perform(
            get("/tccs").param("canReview", "true").header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${tcc.id}')]").doesNotExist())
    }

    private fun abrirTcc(): Tcc {
        val agora = OffsetDateTime.now()
        return tccRepository.save(
            Tcc.abrir(
                Uuids.v7(),
                alunoId,
                cursoId,
                TITULO,
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 10, 3),
                agora,
                listOf(MembroBancaTcc(Uuids.v7(), orientadorId, PapelBancaTcc.ORIENTADOR)),
            ),
        )
    }

    private fun garantirCurso(agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(CODIGO)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(
            Curso(Uuids.v7(), "TADS IT TCC", "TADTCC", CODIGO, null, 120, true, agora, agora),
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

    private fun contar(tabela: String, tipo: String): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from $tabela where tipo = ?",
            Int::class.java,
            tipo,
        ) ?: 0

    private fun contarTabela(tabela: String): Int =
        jdbcTemplate.queryForObject("select count(*) from $tabela", Int::class.java) ?: 0

    companion object {
        private const val EMAIL_ALUNO = "it.tcc.aluno@ufpr.br"
        private const val EMAIL_OUTRO = "it.tcc.outro@ufpr.br"
        private const val EMAIL_PROFESSOR = "it.tcc.prof@ufpr.br"
        private const val EMAIL_ALHEIO = "it.tcc.alheio@ufpr.br"
        private const val EMAIL_SEMCAP = "it.tcc.semcap@ufpr.br"
        private const val GRR_ALUNO = "GRR20249201"
        private const val GRR_OUTRO = "GRR20249202"
        private const val GRR_PROFESSOR = "GRR20249203"
        private const val GRR_ALHEIO = "GRR20249204"
        private const val GRR_SEMCAP = "GRR20249205"
        private const val CODIGO = "TADS-TCC-IT"
        private const val TITULO = "Plataforma de secretaria acadêmica para o SEPT"
        private val PDF = "%PDF-1.4\n".toByteArray(Charsets.US_ASCII)

        private fun pdf(nome: String, contentType: String, bytes: ByteArray): MockMultipartFile =
            MockMultipartFile("arquivo", nome, contentType, bytes)
    }
}
