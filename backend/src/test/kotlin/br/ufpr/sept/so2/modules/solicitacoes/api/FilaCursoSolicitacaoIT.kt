package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FilaCursoSolicitacaoIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var tipoSolicitacaoRepository: TipoSolicitacaoRepository

    @Autowired
    private lateinit var cursoRepository: CursoRepository

    @Autowired
    private lateinit var cursoSecretarioRepository: CursoSecretarioRepository

    @Autowired
    private lateinit var alunoRepository: AlunoRepository

    @Autowired
    private lateinit var solicitacaoRepository: SolicitacaoRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var profId: UUID

    @BeforeEach
    fun seed() {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val agora = OffsetDateTime.now()
            if (tipoSolicitacaoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty) {
                tipoSolicitacaoRepository.save(DeclaracaoSimplesSeed.tipo(agora))
            }
            val sec = usuariosIt.criarUsuario(EMAIL_SEC, "GRR20247201", AUTHORITIES_SEC)
            val prof = usuariosIt.criarUsuario(EMAIL_PROF, "GRR20247202", listOf("request.deliberate"))
            profId = prof.id
            val aluno = usuariosIt.criarUsuario(EMAIL_ALUNO, "GRR20247203", AUTHORITIES_ALUNO)
            val alunoOutro = usuariosIt.criarUsuario(EMAIL_ALUNO_OUTRO, "GRR20247204", AUTHORITIES_ALUNO)
            val cursoSec = curso("Curso Fila Sec", "FLS1", "FILA-SEC", agora)
            val cursoOutro = curso("Curso Fila Outro", "FLS2", "FILA-OUT", agora)
            cursoSecretarioRepository.replaceAll(cursoSec.id, listOf(sec.id))
            alunoAcademico(aluno.grr!!.value, aluno.emailInstitucional.value, cursoSec.id, agora)
            alunoAcademico(alunoOutro.grr!!.value, alunoOutro.emailInstitucional.value, cursoOutro.id, agora)
        }
    }

    @Test
    fun filaCentralFiltraPorCursoEExpoeSlaEBulkAssign() {
        val idNoEscopo = abrir(EMAIL_ALUNO)
        val idFora = abrir(EMAIL_ALUNO_OUTRO)
        val tokenSec = usuariosIt.login(EMAIL_SEC)

        mockMvc.perform(
            get("/requests")
                .param("estado", "ABERTA")
                .header("Authorization", "Bearer $tokenSec"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", hasItem(idNoEscopo)))
            .andExpect(jsonPath("$.content[*].id", not(hasItem(idFora))))
            .andExpect(jsonPath("$.content[0].slaStatus").exists())
            .andExpect(jsonPath("$.content[0]._links.bulk_assign").value("/requests/bulk"))
            .andExpect(jsonPath("$.deliberadores[*].id", hasItem(profId.toString())))

        mockMvc.perform(
            get("/requests")
                .param("curso", Uuids.v7().toString())
                .header("Authorization", "Bearer $tokenSec"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun bulkAtribuiDeliberadorECsvAtrasados() {
        val id = abrir(EMAIL_ALUNO)
        forcarAtraso(UUID.fromString(id))
        val tokenSec = usuariosIt.login(EMAIL_SEC)

        mockMvc.perform(
            patch("/requests/bulk")
                .header("Authorization", "Bearer $tokenSec")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ids":["$id"],"deliberadorId":"$profId"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].deliberadorId").value(profId.toString()))
            .andExpect(jsonPath("$[0].deliberadorNome").isNotEmpty)

        mockMvc.perform(
            get("/requests")
                .param("slaBreached", "true")
                .header("Authorization", "Bearer $tokenSec"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", hasItem(id)))
            .andExpect(jsonPath("$.content[0].slaStatus").value("danger"))

        mockMvc.perform(
            get("/requests")
                .param("slaBreached", "true")
                .param("format", "csv")
                .header("Authorization", "Bearer $tokenSec"),
        )
            .andExpect(status().isOk)
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("atrasados-")))
            .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv")))
            .andExpect(content().string(containsString("Número,Tipo,Aluno,GRR,Curso,Estado,Deliberador")))
            .andExpect(content().string(containsString("GRR20247203")))
    }

    @Test
    fun alunoNaoAcessaBulkNemRecebeDeliberadores() {
        abrir(EMAIL_ALUNO)
        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(
            get("/requests")
                .param("solicitante", "me")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.deliberadores").doesNotExist())

        mockMvc.perform(
            patch("/requests/bulk")
                .header("Authorization", "Bearer $tokenAluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ids":["${Uuids.v7()}"],"deliberadorId":"$profId"}"""),
        )
            .andExpect(status().isForbidden)
    }

    private fun abrir(email: String): String {
        val token = usuariosIt.login(email)
        val created = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadNova()),
        )
            .andExpect(status().isCreated)
            .andReturn()
        return ItJson.text(created.response.contentAsString, "id")
    }

    private fun forcarAtraso(id: UUID) {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val atual = solicitacaoRepository.findById(id).orElseThrow()
            val atrasada = Solicitacao(
                atual.id,
                atual.tipoId,
                atual.tipoCodigo,
                atual.tipoNome,
                atual.tipoVersao,
                atual.solicitanteId,
                atual.protocolo,
                atual.estado,
                atual.payloadJson,
                atual.formSchemaSnapshot,
                atual.workflowSnapshot,
                OffsetDateTime.now().minusDays(3),
                atual.hashSha256,
                atual.deliberadorId,
                atual.eventos,
                atual.createdAt,
                OffsetDateTime.now(),
            )
            solicitacaoRepository.save(atrasada)
        }
    }

    private fun curso(nome: String, sigla: String, codigo: String, agora: OffsetDateTime): Curso {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            return existente.get()
        }
        return cursoRepository.save(Curso(Uuids.v7(), nome, sigla, codigo, null, 120, true, agora, agora))
    }

    private fun alunoAcademico(grr: String, email: String, idCurso: UUID, agora: OffsetDateTime) {
        if (alunoRepository.findByGrr(grr).isPresent) {
            return
        }
        alunoRepository.save(
            Aluno(
                Uuids.v7(),
                "Aluno fila $grr",
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

    companion object {
        private const val EMAIL_SEC = "it.fila.sec@ufpr.br"
        private const val EMAIL_PROF = "it.fila.prof@ufpr.br"
        private const val EMAIL_ALUNO = "it.fila.aluno@ufpr.br"
        private const val EMAIL_ALUNO_OUTRO = "it.fila.aluno.outro@ufpr.br"
        private val AUTHORITIES_ALUNO = listOf("dashboard.view_own", "request.view_own", "request.open")
        private val AUTHORITIES_SEC = listOf(
            "course.manage",
            "request.view_curso",
            "request.triage",
            "request.deliberate",
        )

        private fun payloadNova(): String =
            """
            {
              "tipoCodigo": "DECLARACAO_SIMPLES",
              "payload": {
                "finalidade": "Comprovação de vínculo",
                "observacao": "Fila central F5.2"
              }
            }
            """.trimIndent()
    }
}
