package br.ufpr.sept.so2.modules.comunicacao.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.comunicacao.application.DespacharOutboxUseCase
import br.ufpr.sept.so2.modules.comunicacao.application.ports.TurmaAudienciaPort
import br.ufpr.sept.so2.modules.comunicacao.application.ports.TurmaGravada
import br.ufpr.sept.so2.modules.comunicacao.domain.CanaisEntrega
import br.ufpr.sept.so2.modules.comunicacao.infrastructure.RecordingMailAdapter
import br.ufpr.sept.so2.modules.iam.application.ports.NotificacaoPreferenciaRepository
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.NotificacaoPreferenciaRegras
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.hamcrest.Matchers.hasItem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ComunicacaoControllerIT.MailConfig::class)
class ComunicacaoControllerIT {
    @TestConfiguration
    class MailConfig {
        @Bean
        @Primary
        fun mailPort(): RecordingMailAdapter = RecordingMailAdapter()
    }

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
    private lateinit var turmaPort: TurmaAudienciaPort

    @Autowired
    private lateinit var preferenciaRepository: NotificacaoPreferenciaRepository

    @Autowired
    private lateinit var despacharOutboxUseCase: DespacharOutboxUseCase

    @Autowired
    private lateinit var mailPort: RecordingMailAdapter

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var professorId: UUID
    private lateinit var alunoId: UUID
    private lateinit var cursoId: UUID
    private lateinit var outroCursoId: UUID
    private lateinit var turmaId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        professorId = usuariosIt.criarUsuario(
            EMAIL_PROFESSOR,
            GRR_PROFESSOR,
            listOf("communication.read", "communication.publish_class"),
        ).id
        alunoId = usuariosIt.criarUsuario(EMAIL_ALUNO, GRR_ALUNO, listOf("communication.read")).id
        usuariosIt.criarUsuario(EMAIL_OUTRO, GRR_OUTRO, listOf("communication.read"))
        usuariosIt.criarUsuario(EMAIL_SEMCAP, GRR_SEMCAP, listOf("dashboard.view_own"))
        cursoId = garantirCurso("TADS Hub IT", "COMHUB", CODIGO, professorId, agora)
        outroCursoId = garantirCurso("Outro Hub IT", "COMHUB2", CODIGO_OUTRO, UUID.randomUUID(), agora)
        criarAluno("Aluno Hub", GRR_ALUNO, EMAIL_ALUNO, cursoId, agora)
        criarAluno("Outro Hub", GRR_OUTRO, EMAIL_OUTRO, cursoId, agora)
        turmaId = garantirTurma()
        preferenciaRepository.save(NotificacaoPreferenciaRegras.padrao(alunoId, agora))
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/communications")).andExpect(status().isUnauthorized)
        mockMvc.perform(post("/communications").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityRecebe403() {
        val token = usuariosIt.login(EMAIL_SEMCAP)
        mockMvc.perform(get("/communications").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
        mockMvc.perform(
            post("/communications")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(turmaId)),
        ).andExpect(status().isForbidden)
        val aluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(get("/communications/audiencias").header("Authorization", "Bearer $aluno"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun audienciaForaDoEscopoNaoEnfileira() {
        val token = usuariosIt.login(EMAIL_PROFESSOR)
        val outbox = contarOutbox()
        val comunicacoes = contarTabela("comunicacao")
        mockMvc.perform(
            post("/communications")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoCurso(outroCursoId)),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value("https://secretariaonline.ufpr.br/errors/validation-error"))
            .andExpect(jsonPath("$.detail").value("Audiência fora do escopo."))
        mockMvc.perform(
            post("/communications")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoUniversidade()),
        ).andExpect(status().isUnprocessableEntity)
        assertEquals(outbox, contarOutbox())
        assertEquals(comunicacoes, contarTabela("comunicacao"))
    }

    @Test
    fun publicarEnfileiraOutboxNaMesmaTransacaoEOutroAlunoNaoMarcaLido() {
        mailPort.limpar()
        val tokenProfessor = usuariosIt.login(EMAIL_PROFESSOR)
        val outbox = contarOutbox()
        val titulo = "Aviso da turma IT"
        val resposta = mockMvc.perform(
            post("/communications")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(turmaId, titulo)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.tipo").value("TURMA"))
            .andReturn()
            .response
            .contentAsString
        val id = br.ufpr.sept.so2.shared.ItJson.text(resposta, "id")
        assertEquals(outbox + 1, contarOutbox())
        assertEquals(0, contarEntregas(id))
        val payloads = jdbcTemplate.queryForList(
            "select payload from outbox_event where tipo = ?",
            String::class.java,
            "comunicacao.published",
        )
        assertTrue(payloads.any { it.contains(id) })

        mockMvc.perform(get("/communications").header("Authorization", "Bearer $tokenProfessor"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='$id')].titulo", hasItem(titulo)))
            .andExpect(jsonPath("$.content[?(@.id=='$id')]._links['marcar-lido']").doesNotExist())

        despacharOutboxUseCase.execute()
        assertEquals(1, contarEntregas(id))
        assertTrue(mailPort.mensagens.any { it.to == EMAIL_ALUNO && it.subject == titulo })
        assertFalse(mailPort.mensagens.any { it.to == EMAIL_OUTRO })

        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)
        val tokenOutro = usuariosIt.login(EMAIL_OUTRO)
        mockMvc.perform(get("/communications").header("Authorization", "Bearer $tokenAluno"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='$id')]._links['marcar-lido']", hasItem("/communications/$id/read")))
            .andExpect(jsonPath("$.content[?(@.id=='$id')].lida", hasItem(false)))
        mockMvc.perform(get("/communications").header("Authorization", "Bearer $tokenOutro"))
            .andExpect(jsonPath("$.content[?(@.id=='$id')]").doesNotExist())

        mockMvc.perform(post("/communications/$id/read").header("Authorization", "Bearer $tokenOutro"))
            .andExpect(status().isNotFound)
        mockMvc.perform(get("/communications").header("Authorization", "Bearer $tokenAluno"))
            .andExpect(jsonPath("$.content[?(@.id=='$id')].lida", hasItem(false)))

        mockMvc.perform(post("/communications/$id/read").header("Authorization", "Bearer $tokenAluno"))
            .andExpect(status().isNoContent)
        mockMvc.perform(post("/communications/$id/read").header("Authorization", "Bearer $tokenAluno"))
            .andExpect(status().isNoContent)
        mockMvc.perform(get("/communications").header("Authorization", "Bearer $tokenAluno"))
            .andExpect(jsonPath("$.content[?(@.id=='$id')].lida", hasItem(true)))
            .andExpect(jsonPath("$.content[?(@.id=='$id')]._links['marcar-lido']").doesNotExist())
    }

    @Test
    fun audienciasSoTemTurmaECursoDoProfessor() {
        val token = usuariosIt.login(EMAIL_PROFESSOR)
        val corpo = mockMvc.perform(get("/communications/audiencias").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.tipo=='TURMA')].id", hasItem(turmaId.toString())))
            .andExpect(jsonPath("$.content[?(@.tipo=='CURSO')].id", hasItem(cursoId.toString())))
            .andReturn()
            .response
            .contentAsString
        assertFalse(corpo.contains("UNIVERSIDADE"))
        assertFalse(corpo.contains("todos os alunos", ignoreCase = true))
        assertFalse(corpo.contains(outroCursoId.toString()))
    }

    @Test
    fun criticalNaoESuprimidoPeloDnd() {
        mailPort.limpar()
        val agora = LocalTime.now(CanaisEntrega.ZONA)
        val preferencia = NotificacaoPreferenciaRegras.padrao(alunoId, OffsetDateTime.now())
        preferencia.dndInicio = agora.minusHours(1)
        preferencia.dndFim = agora.plusHours(1)
        preferenciaRepository.save(preferencia)
        val token = usuariosIt.login(EMAIL_PROFESSOR)
        try {
            val baixo = "Aviso baixo DND"
            mockMvc.perform(
                post("/communications")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(corpo(turmaId, baixo, "LOW")),
            ).andExpect(status().isCreated)
            despacharOutboxUseCase.execute()
            assertFalse(mailPort.mensagens.any { it.subject == baixo })
            mockMvc.perform(get("/communications").header("Authorization", "Bearer ${usuariosIt.login(EMAIL_ALUNO)}"))
                .andExpect(jsonPath("$.content[?(@.titulo=='$baixo')]").exists())

            val critico = "Aviso critico DND"
            mockMvc.perform(
                post("/communications")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(corpo(turmaId, critico, "CRITICAL")),
            ).andExpect(status().isCreated)
            despacharOutboxUseCase.execute()
            assertTrue(mailPort.mensagens.any { it.to == EMAIL_ALUNO && it.subject == critico })
        } finally {
            preferenciaRepository.save(NotificacaoPreferenciaRegras.padrao(alunoId, OffsetDateTime.now()))
        }
    }

    private fun garantirTurma(): UUID {
        val existente = turmaPort.findByProfessorECodigo(professorId, "HUBIT")
        val turma = existente ?: turmaPort.salvar(TurmaGravada(Uuids.v7(), cursoId, professorId, "HUBIT", "Hub IT"))
        val aluno = alunoRepository.findByEmailInstitucional(EMAIL_ALUNO).orElseThrow()
        turmaPort.matricularSeAusente(turma.id, aluno.id)
        return turma.id
    }

    private fun garantirCurso(nome: String, sigla: String, codigo: String, coordenador: UUID, agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(
            Curso(Uuids.v7(), nome, sigla, codigo, coordenador, 120, true, agora, agora),
        ).id
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

    private fun corpo(turma: UUID, titulo: String = "Aviso", prioridade: String = "MEDIUM") = """
        {"titulo":"$titulo","corpo":"Corpo do aviso","audiencia":{"tipo":"TURMA","id":"$turma"},"prioridade":"$prioridade"}
        """.trimIndent()

    private fun corpoCurso(curso: UUID) = """
        {"titulo":"Fora","corpo":"Nao deve gravar","audiencia":{"tipo":"CURSO","id":"$curso"},"prioridade":"LOW"}
        """.trimIndent()

    private fun corpoUniversidade() = """
        {"titulo":"Todos","corpo":"Nao existe","audiencia":{"tipo":"UNIVERSIDADE","id":"${UUID.randomUUID()}"},"prioridade":"HIGH"}
        """.trimIndent()

    private fun contarOutbox(): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from outbox_event where tipo = ?",
            Int::class.java,
            "comunicacao.published",
        ) ?: 0

    private fun contarEntregas(comunicacaoId: String): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from comunicacao_entrega where id_comunicacao = ?",
            Int::class.java,
            UUID.fromString(comunicacaoId),
        ) ?: 0

    private fun contarTabela(tabela: String): Int =
        jdbcTemplate.queryForObject("select count(*) from $tabela", Int::class.java) ?: 0

    companion object {
        private const val EMAIL_PROFESSOR = "it.comunicacao.prof@ufpr.br"
        private const val EMAIL_ALUNO = "it.comunicacao.aluno@ufpr.br"
        private const val EMAIL_OUTRO = "it.comunicacao.outro@ufpr.br"
        private const val EMAIL_SEMCAP = "it.comunicacao.semcap@ufpr.br"
        private const val GRR_PROFESSOR = "GRR20249301"
        private const val GRR_ALUNO = "GRR20249302"
        private const val GRR_OUTRO = "GRR20249303"
        private const val GRR_SEMCAP = "GRR20249304"
        private const val CODIGO = "COM-HUB-IT"
        private const val CODIGO_OUTRO = "COM-HUB-OUTRO"
    }
}
