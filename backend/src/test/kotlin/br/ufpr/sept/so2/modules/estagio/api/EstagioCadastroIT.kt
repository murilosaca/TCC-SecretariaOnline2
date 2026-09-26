package br.ufpr.sept.so2.modules.estagio.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.comunicacao.application.NoOpOutboxHandler
import br.ufpr.sept.so2.modules.estagio.application.ports.CoeMembroPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.DocumentoEstagio
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.EstagioSituacao
import br.ufpr.sept.so2.modules.estagio.domain.TipoDocumentoEstagio
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstagioCadastroIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher

    @Autowired
    private lateinit var cursoRepository: CursoRepository

    @Autowired
    private lateinit var alunoRepository: AlunoRepository

    @Autowired
    private lateinit var cursoSecretarioRepository: CursoSecretarioRepository

    @Autowired
    private lateinit var estagioRepository: EstagioRepository

    @Autowired
    private lateinit var coeMembroPort: CoeMembroPort

    @Autowired
    private lateinit var noOpOutboxHandler: NoOpOutboxHandler

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoId: UUID
    private lateinit var cursoForaId: UUID
    private lateinit var alunoId: UUID
    private lateinit var alunoForaId: UUID
    private lateinit var professorId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        cursoId = garantirCurso("TADS Cadastro IT", "TADCAD", CODIGO, agora)
        cursoForaId = garantirCurso("Outro Cadastro IT", "OUTCAD", CODIGO_FORA, agora)
        usuariosIt.criarUsuario(EMAIL_ALUNO, GRR_ALUNO, listOf("dashboard.view_own", "internship.view_own"))
        alunoId = criarAluno("Aluno Cadastro IT", GRR_ALUNO, EMAIL_ALUNO, cursoId, agora)
        usuariosIt.criarUsuario(EMAIL_OUTRO, GRR_OUTRO, listOf("dashboard.view_own", "internship.view_own"))
        criarAluno("Outro Cadastro IT", GRR_OUTRO, EMAIL_OUTRO, cursoId, agora)
        alunoForaId = criarAluno("Fora Cadastro IT", GRR_FORA, EMAIL_FORA, cursoForaId, agora)
        professorId = usuariosIt.criarUsuario(
            EMAIL_PROFESSOR,
            GRR_PROFESSOR,
            listOf("dashboard.view_own", "internship.review"),
        ).id
        val secretaria = usuariosIt.criarUsuario(
            EMAIL_SECRETARIA,
            GRR_SECRETARIA,
            listOf("internship.manage", "user.manage_students"),
        )
        cursoSecretarioRepository.adicionarSeAusente(cursoId, secretaria.id)
        coeMembroPort.adicionarSeAusente(cursoId, professorId)
    }

    @Test
    fun anonimoRecebe401EQuemNaoGerenciaRecebe403() {
        val corpo = corpo(alunoId)
        mockMvc.perform(post("/estagios").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isUnauthorized)
        mockMvc.perform(get("/estagios").param("escopo", "cursos"))
            .andExpect(status().isUnauthorized)

        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(post("/estagios").header("Authorization", "Bearer $tokenAluno").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/estagios").param("escopo", "cursos").header("Authorization", "Bearer $tokenAluno"))
            .andExpect(status().isForbidden)

        val tokenProfessor = usuariosIt.login(EMAIL_PROFESSOR)
        mockMvc.perform(post("/estagios").header("Authorization", "Bearer $tokenProfessor").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isForbidden)

        val tokenSecretaria = usuariosIt.login(EMAIL_SECRETARIA)
        mockMvc.perform(get("/estagios").param("aluno", "me").header("Authorization", "Bearer $tokenSecretaria"))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/estagios").param("canReview", "true").header("Authorization", "Bearer $tokenSecretaria"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun alunoInexistenteEForaDoEscopoSao404() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        val inexistente = detalhe(
            mockMvc.perform(
                post("/estagios")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(corpo(Uuids.v7())),
            )
                .andExpect(status().isNotFound)
                .andReturn()
                .response
                .contentAsString,
        )
        val fora = detalhe(
            mockMvc.perform(
                post("/estagios")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(corpo(alunoForaId)),
            )
                .andExpect(status().isNotFound)
                .andReturn()
                .response
                .contentAsString,
        )
        assertEquals(inexistente, fora)
    }

    @Test
    fun secretariaRegistraSemOrientadorAlunoVeUploadECoeListaNoPool() {
        assertTrue("estagio.registrado" in noOpOutboxHandler.tipos)
        assertTrue("estagio.atualizado" in noOpOutboxHandler.tipos)

        val token = usuariosIt.login(EMAIL_SECRETARIA)
        val antesRegistro = contar("estagio.registrado")
        val antesOutbox = contarOutbox("estagio.registrado")
        val criado = mockMvc.perform(
            post("/estagios")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoId)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.situacao").value("ATIVO"))
            .andExpect(jsonPath("$.idOrientador").doesNotExist())
            .andExpect(jsonPath("$.idAluno").value(alunoId.toString()))
            .andExpect(jsonPath("$.empresa").value(EMPRESA))
            .andExpect(jsonPath("$.documentos[?(@.tipo=='TCE')].estado").value("PENDENTE"))
            .andExpect(jsonPath("$.documentos[?(@.tipo=='RELATORIO_FINAL')].estado").value("PENDENTE"))
            .andExpect(jsonPath("$.documentos[?(@.tipo=='TCE')].obrigatorio").value(true))
            .andExpect(jsonPath("$._links.novo").doesNotExist())
            .andReturn()
            .response
            .contentAsString
        val id = objectMapper.readTree(criado).get("id").asText()
        assertEquals(antesRegistro + 1, contar("estagio.registrado"))
        assertEquals(antesOutbox + 1, contarOutbox("estagio.registrado"))

        mockMvc.perform(get("/estagios").param("escopo", "cursos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.novo").value("/estagios"))
            .andExpect(jsonPath("$.content[?(@.id=='$id')].idOrientador").doesNotExist())
            .andExpect(jsonPath("$.content[?(@.id=='$id')]._links.editar").exists())

        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(get("/estagios").param("aluno", "me").header("Authorization", "Bearer $tokenAluno"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.novo").doesNotExist())
            .andExpect(jsonPath("$.content[?(@.id=='$id')]").exists())
        mockMvc.perform(get("/estagios/$id").header("Authorization", "Bearer $tokenAluno"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.idOrientador").doesNotExist())
            .andExpect(jsonPath("$.documentos[?(@.tipo=='TCE')]._links.upload").exists())
            .andExpect(jsonPath("$.documentos[?(@.tipo=='RELATORIO_FINAL')]._links.upload").exists())

        val tokenOutro = usuariosIt.login(EMAIL_OUTRO)
        mockMvc.perform(get("/estagios/$id").header("Authorization", "Bearer $tokenOutro"))
            .andExpect(status().isNotFound)

        val tokenProfessor = usuariosIt.login(EMAIL_PROFESSOR)
        mockMvc.perform(get("/estagios").param("canReview", "true").header("Authorization", "Bearer $tokenProfessor"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='$id')]").doesNotExist())
        mockMvc.perform(get("/comissoes/coe").header("Authorization", "Bearer $tokenProfessor"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='$id')].idOrientador").doesNotExist())
            .andExpect(jsonPath("$.content[?(@.id=='$id')].noPool").value(true))

        val antesAtualizacao = contar("estagio.atualizado")
        mockMvc.perform(
            put("/estagios/$id")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoId, "Empresa Editada")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.empresa").value("Empresa Editada"))
            .andExpect(jsonPath("$.idOrientador").doesNotExist())
            .andExpect(jsonPath("$.situacao").value("ATIVO"))
        assertEquals(antesAtualizacao + 1, contar("estagio.atualizado"))
        assertTrue(contarOutbox("estagio.atualizado") >= 1)
    }

    @Test
    fun concluidoEEstagioDeOutroCursoNaoSaoEditaveis() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        val agora = OffsetDateTime.now()
        val concluido = estagioRepository.save(
            Estagio(
                Uuids.v7(),
                alunoId,
                cursoId,
                null,
                "Concluído IT",
                SUPERVISOR,
                LocalDate.of(2026, 1, 2),
                LocalDate.of(2026, 6, 30),
                EstagioSituacao.CONCLUIDO,
                agora,
                agora,
                listOf(
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.TCE),
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.RELATORIO_FINAL),
                ),
            ),
        )
        val fora = estagioRepository.save(
            Estagio.abrir(
                Uuids.v7(),
                alunoForaId,
                cursoForaId,
                null,
                "Fora do escopo",
                SUPERVISOR,
                LocalDate.of(2026, 3, 2),
                LocalDate.of(2026, 11, 30),
                agora,
                listOf(
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.TCE),
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.RELATORIO_FINAL),
                ),
            ),
        )
        mockMvc.perform(
            put("/estagios/${concluido.id}")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoId, "Não deve gravar")),
        )
            .andExpect(status().isConflict)
        mockMvc.perform(
            put("/estagios/${fora.id}")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoForaId, "Não deve gravar")),
        )
            .andExpect(status().isNotFound)
        mockMvc.perform(get("/estagios").param("escopo", "cursos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${concluido.id}')]._links.editar").doesNotExist())
            .andExpect(jsonPath("$.content[?(@.id=='${fora.id}')]").doesNotExist())
        assertEquals("Concluído IT", estagioRepository.findById(concluido.id)?.empresa)
    }

    private fun corpo(aluno: UUID, empresa: String = EMPRESA): String =
        """{"alunoId":"$aluno","empresa":"$empresa","supervisor":"$SUPERVISOR","inicio":"2026-04-01","fim":"2026-10-30"}"""

    private fun detalhe(json: String): String = objectMapper.readTree(json).get("detail").asText()

    private fun contar(tipo: String): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from audit_log where tipo = ?",
            Int::class.java,
            tipo,
        ) ?: 0

    private fun contarOutbox(tipo: String): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from outbox_event where tipo = ?",
            Int::class.java,
            tipo,
        ) ?: 0

    private fun garantirCurso(nome: String, sigla: String, codigo: String, agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(Curso(Uuids.v7(), nome, sigla, codigo, null, 120, true, agora, agora)).id
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

    companion object {
        private const val EMAIL_ALUNO = "it.estagio.cadastro.aluno@ufpr.br"
        private const val EMAIL_OUTRO = "it.estagio.cadastro.outro@ufpr.br"
        private const val EMAIL_FORA = "it.estagio.cadastro.fora@ufpr.br"
        private const val EMAIL_PROFESSOR = "it.estagio.cadastro.prof@ufpr.br"
        private const val EMAIL_SECRETARIA = "it.estagio.cadastro.sec@ufpr.br"
        private const val GRR_ALUNO = "GRR20249411"
        private const val GRR_OUTRO = "GRR20249412"
        private const val GRR_FORA = "GRR20249413"
        private const val GRR_PROFESSOR = "GRR20249414"
        private const val GRR_SECRETARIA = "GRR20249415"
        private const val CODIGO = "TADS-EST-CAD"
        private const val CODIGO_FORA = "TADS-EST-FORA"
        private const val EMPRESA = "Empresa Cadastro IT"
        private const val SUPERVISOR = "Marina Supervisora"
    }
}
