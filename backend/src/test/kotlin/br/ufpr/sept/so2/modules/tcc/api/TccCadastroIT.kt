package br.ufpr.sept.so2.modules.tcc.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.comunicacao.application.NoOpOutboxHandler
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.MembroBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.PapelBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import br.ufpr.sept.so2.modules.tcc.domain.TccSituacao
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
class TccCadastroIT {

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
    private lateinit var tccRepository: TccRepository

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
        cursoId = garantirCurso("TADS TCC Cadastro IT", "TADTCAD", CODIGO, agora)
        cursoForaId = garantirCurso("Outro TCC Cadastro IT", "OUTTCAD", CODIGO_FORA, agora)
        usuariosIt.criarUsuario(EMAIL_ALUNO, GRR_ALUNO, listOf("dashboard.view_own", "tcc.view_own"))
        alunoId = criarAluno("Aluno TCC Cadastro IT", GRR_ALUNO, EMAIL_ALUNO, cursoId, agora)
        usuariosIt.criarUsuario(EMAIL_OUTRO, GRR_OUTRO, listOf("dashboard.view_own", "tcc.view_own"))
        criarAluno("Outro TCC Cadastro IT", GRR_OUTRO, EMAIL_OUTRO, cursoId, agora)
        alunoForaId = criarAluno("Fora TCC Cadastro IT", GRR_FORA, EMAIL_FORA, cursoForaId, agora)
        professorId = usuariosIt.criarUsuario(
            EMAIL_PROFESSOR,
            GRR_PROFESSOR,
            listOf("dashboard.view_own", "tcc.review"),
        ).id
        val secretaria = usuariosIt.criarUsuario(
            EMAIL_SECRETARIA,
            GRR_SECRETARIA,
            listOf("tcc.manage", "user.manage_students", "course.manage"),
        )
        cursoSecretarioRepository.adicionarSeAusente(cursoId, secretaria.id)
    }

    @Test
    fun anonimoRecebe401EQuemNaoGerenciaRecebe403() {
        val corpo = corpo(alunoId, professorId)
        mockMvc.perform(post("/tccs").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isUnauthorized)
        mockMvc.perform(get("/tccs").param("escopo", "cursos"))
            .andExpect(status().isUnauthorized)

        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(post("/tccs").header("Authorization", "Bearer $tokenAluno").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/tccs").param("escopo", "cursos").header("Authorization", "Bearer $tokenAluno"))
            .andExpect(status().isForbidden)

        val tokenProfessor = usuariosIt.login(EMAIL_PROFESSOR)
        mockMvc.perform(post("/tccs").header("Authorization", "Bearer $tokenProfessor").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isForbidden)

        val tokenSecretaria = usuariosIt.login(EMAIL_SECRETARIA)
        mockMvc.perform(get("/tccs").param("aluno", "me").header("Authorization", "Bearer $tokenSecretaria"))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/tccs").param("canReview", "true").header("Authorization", "Bearer $tokenSecretaria"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun alunoInexistenteForaDoEscopoEMembroInexistente() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        val inexistente = detalhe(
            mockMvc.perform(
                post("/tccs")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(corpo(Uuids.v7(), professorId)),
            )
                .andExpect(status().isNotFound)
                .andReturn()
                .response
                .contentAsString,
        )
        val fora = detalhe(
            mockMvc.perform(
                post("/tccs")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(corpo(alunoForaId, professorId)),
            )
                .andExpect(status().isNotFound)
                .andReturn()
                .response
                .contentAsString,
        )
        assertEquals(inexistente, fora)

        mockMvc.perform(
            post("/tccs")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoId, Uuids.v7())),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.detail").value("Um ou mais membros da banca não existem."))
    }

    @Test
    fun secretariaRegistraComBancaAlunoVeESegundoAtivoConflita() {
        assertTrue("tcc.registrado" in noOpOutboxHandler.tipos)
        assertTrue("tcc.atualizado" in noOpOutboxHandler.tipos)

        val token = usuariosIt.login(EMAIL_SECRETARIA)
        val antesRegistro = contar("tcc.registrado")
        val antesOutbox = contarOutbox("tcc.registrado")
        val criado = mockMvc.perform(
            post("/tccs")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoId, professorId)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.situacao").value("ATIVO"))
            .andExpect(jsonPath("$.estado").value("EM_ELABORACAO"))
            .andExpect(jsonPath("$.idAluno").value(alunoId.toString()))
            .andExpect(jsonPath("$.titulo").value(TITULO))
            .andExpect(jsonPath("$.membros[?(@.papel=='ORIENTADOR')].idUsuario").value(professorId.toString()))
            .andExpect(jsonPath("$._links.editar").exists())
            .andExpect(jsonPath("$._links.novo").doesNotExist())
            .andReturn()
            .response
            .contentAsString
        val id = objectMapper.readTree(criado).get("id").asText()
        assertEquals(antesRegistro + 1, contar("tcc.registrado"))
        assertEquals(antesOutbox + 1, contarOutbox("tcc.registrado"))

        mockMvc.perform(get("/tccs").param("escopo", "cursos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.novo").value("/tccs"))
            .andExpect(jsonPath("$.content[?(@.id=='$id')]._links.editar").exists())

        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(get("/tccs").param("aluno", "me").header("Authorization", "Bearer $tokenAluno"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.novo").doesNotExist())
            .andExpect(jsonPath("$.content[?(@.id=='$id')]").exists())
        mockMvc.perform(get("/tccs/$id").header("Authorization", "Bearer $tokenAluno"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.membros[0].papel").value("ORIENTADOR"))
            .andExpect(jsonPath("$._links.upload-final").exists())

        mockMvc.perform(
            post("/tccs")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoId, professorId, "Outro título ativo")),
        )
            .andExpect(status().isConflict)

        val antesAtualizacao = contar("tcc.atualizado")
        mockMvc.perform(
            put("/tccs/$id")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoId, professorId, "Título editado")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.titulo").value("Título editado"))
            .andExpect(jsonPath("$.situacao").value("ATIVO"))
        assertEquals(antesAtualizacao + 1, contar("tcc.atualizado"))
        assertTrue(contarOutbox("tcc.atualizado") >= 1)
    }

    @Test
    fun concluidoETccDeOutroCursoNaoSaoEditaveis() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        val agora = OffsetDateTime.now()
        val concluido = tccRepository.save(
            Tcc(
                Uuids.v7(),
                alunoId,
                cursoId,
                "Concluído IT",
                TccSituacao.CONCLUIDO,
                TccEstado.APROVADO,
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 10, 3),
                null,
                null,
                null,
                null,
                null,
                agora,
                agora,
                listOf(MembroBancaTcc(Uuids.v7(), professorId, PapelBancaTcc.ORIENTADOR)),
            ),
        )
        val fora = tccRepository.save(
            Tcc.abrir(
                Uuids.v7(),
                alunoForaId,
                cursoForaId,
                "Fora do escopo",
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 10, 3),
                agora,
                listOf(MembroBancaTcc(Uuids.v7(), professorId, PapelBancaTcc.ORIENTADOR)),
            ),
        )
        mockMvc.perform(
            put("/tccs/${concluido.id}")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoId, professorId, "Não deve gravar")),
        )
            .andExpect(status().isConflict)
        mockMvc.perform(
            put("/tccs/${fora.id}")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(alunoForaId, professorId, "Não deve gravar")),
        )
            .andExpect(status().isNotFound)
        mockMvc.perform(get("/tccs").param("escopo", "cursos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${concluido.id}')]._links.editar").doesNotExist())
            .andExpect(jsonPath("$.content[?(@.id=='${fora.id}')]").doesNotExist())
        assertEquals("Concluído IT", tccRepository.findById(concluido.id)?.titulo)
    }

    private fun corpo(aluno: UUID, orientador: UUID, titulo: String = TITULO): String =
        """{"alunoId":"$aluno","titulo":"$titulo","dataDefesa":"2026-11-12","dataEntrega":"2026-10-03","membros":[{"idUsuario":"$orientador","papel":"ORIENTADOR"}]}"""

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
        private const val EMAIL_ALUNO = "it.tcc.cadastro.aluno@ufpr.br"
        private const val EMAIL_OUTRO = "it.tcc.cadastro.outro@ufpr.br"
        private const val EMAIL_FORA = "it.tcc.cadastro.fora@ufpr.br"
        private const val EMAIL_PROFESSOR = "it.tcc.cadastro.prof@ufpr.br"
        private const val EMAIL_SECRETARIA = "it.tcc.cadastro.sec@ufpr.br"
        private const val GRR_ALUNO = "GRR20249511"
        private const val GRR_OUTRO = "GRR20249512"
        private const val GRR_FORA = "GRR20249513"
        private const val GRR_PROFESSOR = "GRR20249514"
        private const val GRR_SECRETARIA = "GRR20249515"
        private const val CODIGO = "TADS-TCC-CAD"
        private const val CODIGO_FORA = "TADS-TCC-FORA"
        private const val TITULO = "TCC Cadastro Secretária IT"
    }
}
