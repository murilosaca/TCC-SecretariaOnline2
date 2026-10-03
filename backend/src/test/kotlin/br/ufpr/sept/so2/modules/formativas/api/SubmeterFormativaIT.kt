package br.ufpr.sept.so2.modules.formativas.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.formativas.application.ports.ComissaoMembroPort
import br.ufpr.sept.so2.modules.formativas.application.ports.TipoAtividadeFormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.TipoAtividadeFormativa
import br.ufpr.sept.so2.modules.formativas.domain.TipoComissao
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubmeterFormativaIT {
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
    private lateinit var tipoRepository: TipoAtividadeFormativaRepository

    @Autowired
    private lateinit var comissaoMembroPort: ComissaoMembroPort

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoId: UUID
    private lateinit var tipoId: UUID
    private lateinit var tipoOutroId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        cursoId = garantirCurso(CODIGO, "TADS Comp IT", "TCOMP", agora)
        val outro = garantirCurso(CODIGO_OUTRO, "Outro Comp IT", "OCOMP", agora)
        usuariosIt.criarUsuario(EMAIL_ALUNO, GRR_ALUNO, listOf("formative.view_own", "formative.submit"))
        criarAluno("Aluno Comp", GRR_ALUNO, EMAIL_ALUNO, cursoId, agora)
        usuariosIt.criarUsuario(EMAIL_OUTRO, GRR_OUTRO, listOf("formative.view_own", "formative.submit"))
        criarAluno("Outro Comp", GRR_OUTRO, EMAIL_OUTRO, cursoId, agora)
        usuariosIt.criarUsuario(EMAIL_SEMCAP, GRR_SEMCAP, listOf("formative.view_own"))
        criarAluno("Sem cap", GRR_SEMCAP, EMAIL_SEMCAP, cursoId, agora)
        val caafId = usuariosIt.criarUsuario(EMAIL_CAAF, GRR_CAAF, listOf("formative.review")).id
        comissaoMembroPort.adicionarSeAusente(cursoId, caafId, TipoComissao.CAAF)
        tipoId = garantirTipo(cursoId, "Curso de extensão", agora)
        tipoOutroId = garantirTipo(outro, "Tipo de outro curso", agora)
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/formativas")).andExpect(status().isUnauthorized)
        mockMvc.perform(
            multipart("/formativas")
                .file(pdf())
                .param("tipoId", tipoId.toString())
                .param("cargaHoraria", "4"),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityRecebe403ENaoGanhaNova() {
        val token = usuariosIt.login(EMAIL_SEMCAP)
        mockMvc.perform(get("/formativas").param("audience", "me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.nova").doesNotExist())
        mockMvc.perform(
            multipart("/formativas")
                .file(pdf())
                .param("tipoId", tipoId.toString())
                .param("cargaHoraria", "4")
                .header("Authorization", "Bearer $token"),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun arquivoInvalidoOuTipoForaDoCursoNaoEnfileira() {
        val token = usuariosIt.login(EMAIL_ALUNO)
        val outbox = contarOutbox()
        val antes = contarTabela("formativa")
        mockMvc.perform(
            multipart("/formativas")
                .file(MockMultipartFile("arquivo", "nota.txt", "text/plain", "nao e pdf".toByteArray()))
                .param("tipoId", tipoId.toString())
                .param("cargaHoraria", "4")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value("https://secretariaonline.ufpr.br/errors/validation-error"))
        mockMvc.perform(
            multipart("/formativas")
                .file(pdf())
                .param("tipoId", tipoOutroId.toString())
                .param("cargaHoraria", "4")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.detail").value("Tipo de atividade fora do curso."))
        mockMvc.perform(
            multipart("/formativas")
                .file(pdf())
                .param("tipoId", tipoId.toString())
                .param("cargaHoraria", "0")
                .header("Authorization", "Bearer $token"),
        ).andExpect(status().isUnprocessableEntity)
        assertEquals(outbox, contarOutbox())
        assertEquals(antes, contarTabela("formativa"))
    }

    @Test
    fun submeterNasceAguardandoCaafComOutboxELoteRecusa() {
        val token = usuariosIt.login(EMAIL_ALUNO)
        val outbox = contarOutbox()
        mockMvc.perform(get("/formativas/tipos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='$tipoId')].nome", hasItem("Curso de extensão")))
            .andExpect(jsonPath("$.content[?(@.id=='$tipoOutroId')]").doesNotExist())
        mockMvc.perform(get("/formativas").param("audience", "me").header("Authorization", "Bearer $token"))
            .andExpect(jsonPath("$._links.nova").value("/formativas/nova"))

        val corpo = mockMvc.perform(
            multipart("/formativas")
                .file(pdf())
                .param("tipoId", tipoId.toString())
                .param("cargaHoraria", "8")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.origem").value("COMPROVANTE"))
            .andExpect(jsonPath("$.estado").value("AGUARDANDO_CAAF"))
            .andExpect(jsonPath("$.cargaHoraria").value(8))
            .andExpect(jsonPath("$.idEvento").doesNotExist())
            .andExpect(jsonPath("$._links.comprovante").exists())
            .andReturn()
            .response
            .contentAsString
        val id = ItJson.text(corpo, "id")
        assertEquals(outbox + 1, contarOutbox())
        val storage = jdbcTemplate.queryForObject(
            "select storage_key from formativa where id = ?",
            String::class.java,
            UUID.fromString(id),
        )
        assertTrue(!storage.isNullOrBlank())

        val outro = usuariosIt.login(EMAIL_OUTRO)
        mockMvc.perform(get("/formativas/$id").header("Authorization", "Bearer $outro"))
            .andExpect(status().isNotFound)

        val caaf = usuariosIt.login(EMAIL_CAAF)
        mockMvc.perform(
            get("/formativas").param("canReview", "true").header("Authorization", "Bearer $caaf"),
        ).andExpect(jsonPath("$.content[?(@.id=='$id')].origem", hasItem("COMPROVANTE")))
        mockMvc.perform(
            post("/comissoes/caaf/lote")
                .header("Authorization", "Bearer $caaf")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ids":["$id"],"decisao":"APROVADA"}"""),
        ).andExpect(status().isUnprocessableEntity)
        assertEquals(
            "AGUARDANDO_CAAF",
            jdbcTemplate.queryForObject(
                "select estado from formativa where id = ?",
                String::class.java,
                UUID.fromString(id),
            ),
        )
    }

    private fun pdf() = MockMultipartFile(
        "arquivo",
        "comprovante.pdf",
        MediaType.APPLICATION_PDF_VALUE,
        "%PDF-1.4\n".toByteArray(),
    )

    private fun garantirCurso(codigo: String, nome: String, sigla: String, agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(Curso(Uuids.v7(), nome, sigla, codigo, null, 120, true, agora, agora)).id
    }

    private fun criarAluno(nome: String, grr: String, email: String, idCurso: UUID, agora: OffsetDateTime) {
        if (alunoRepository.findByGrr(grr).isPresent) {
            return
        }
        alunoRepository.save(
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
        )
    }

    private fun garantirTipo(curso: UUID, nome: String, agora: OffsetDateTime): UUID {
        val existente = tipoRepository.findByCursoENome(curso, nome)
        if (existente != null) {
            return existente.id
        }
        return tipoRepository.save(TipoAtividadeFormativa(Uuids.v7(), curso, nome, true, agora, agora)).id
    }

    private fun contarOutbox(): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from outbox_event where tipo = ?",
            Int::class.java,
            "formativas.submitted",
        ) ?: 0

    private fun contarTabela(tabela: String): Int =
        jdbcTemplate.queryForObject("select count(*) from $tabela", Int::class.java) ?: 0

    companion object {
        private const val EMAIL_ALUNO = "it.formcomp.aluno@ufpr.br"
        private const val EMAIL_OUTRO = "it.formcomp.outro@ufpr.br"
        private const val EMAIL_SEMCAP = "it.formcomp.semcap@ufpr.br"
        private const val EMAIL_CAAF = "it.formcomp.caaf@ufpr.br"
        private const val GRR_ALUNO = "GRR20249401"
        private const val GRR_OUTRO = "GRR20249402"
        private const val GRR_SEMCAP = "GRR20249403"
        private const val GRR_CAAF = "GRR20249404"
        private const val CODIGO = "COM-FORM-IT"
        private const val CODIGO_OUTRO = "COM-FORM-OUTRO"
    }
}
