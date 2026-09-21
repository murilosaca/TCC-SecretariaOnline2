package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RequestViewCursoIT {
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
    private lateinit var transactionManager: PlatformTransactionManager

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val agora = OffsetDateTime.now()
            if (tipoSolicitacaoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty) {
                tipoSolicitacaoRepository.save(DeclaracaoSimplesSeed.tipo(agora))
            }
            val sec = usuariosIt.criarUsuario(EMAIL_SEC, "GRR20247141", AUTHORITIES_SEC)
            usuariosIt.criarUsuario(EMAIL_PROF, "GRR20247142", listOf("request.deliberate"))
            val alunoOutro = usuariosIt.criarUsuario(EMAIL_ALUNO_OUTRO, "GRR20247143", AUTHORITIES_ALUNO)
            val cursoSec = curso("Curso Sec", "RVC1", "RVC-SEC", agora)
            val cursoOutro = curso("Curso Outro", "RVC2", "RVC-OUT", agora)
            cursoSecretarioRepository.replaceAll(cursoSec.id, listOf(sec.id))
            alunoAcademico(alunoOutro.grr!!.value, alunoOutro.emailInstitucional.value, cursoOutro.id, agora)
        }
    }

    @Test
    fun secretariaNaoVePedidoDeOutroCursoEProfessorContinuaVendoFila() {
        val tokenAluno = usuariosIt.login(EMAIL_ALUNO_OUTRO)
        val created = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $tokenAluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadNova()),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val id = ItJson.text(created.response.contentAsString, "id")

        val tokenSec = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(
            get("/requests")
                .param("canDeliberate", "true")
                .header("Authorization", "Bearer $tokenSec"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", not(hasItem(id))))

        mockMvc.perform(get("/requests/$id").header("Authorization", "Bearer $tokenSec"))
            .andExpect(status().isNotFound)

        val tokenProf = usuariosIt.login(EMAIL_PROF)
        mockMvc.perform(
            get("/requests")
                .param("canDeliberate", "true")
                .header("Authorization", "Bearer $tokenProf"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", hasItem(id)))
    }

    private fun curso(nome: String, sigla: String, codigo: String, agora: OffsetDateTime): br.ufpr.sept.so2.modules.academico.domain.Curso {
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
                "Aluno outro curso",
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
        private const val EMAIL_SEC = "it.fgac.rvc.sec@ufpr.br"
        private const val EMAIL_PROF = "it.fgac.rvc.prof@ufpr.br"
        private const val EMAIL_ALUNO_OUTRO = "it.fgac.rvc.aluno@ufpr.br"
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
                "observacao": "Escopo por curso"
              }
            }
            """.trimIndent()
    }
}
