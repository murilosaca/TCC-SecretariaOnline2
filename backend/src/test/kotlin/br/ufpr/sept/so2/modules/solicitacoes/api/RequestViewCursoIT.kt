package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
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

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        if (tipoSolicitacaoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty) {
            tipoSolicitacaoRepository.save(DeclaracaoSimplesSeed.tipo(agora))
        }
        val sec = criarUsuario(EMAIL_SEC, "GRR20247141", AUTHORITIES_SEC, agora)
        criarUsuario(EMAIL_PROF, "GRR20247142", listOf("request.deliberate"), agora)
        val alunoOutro = criarUsuario(EMAIL_ALUNO_OUTRO, "GRR20247143", AUTHORITIES_ALUNO, agora)
        val cursoSec = curso("Curso Sec", "RVC1", "RVC-SEC", agora)
        val cursoOutro = curso("Curso Outro", "RVC2", "RVC-OUT", agora)
        cursoSecretarioRepository.replaceAll(cursoSec.id, listOf(sec.id))
        alunoAcademico(alunoOutro.grr!!.value, alunoOutro.emailInstitucional.value, cursoOutro.id, agora)
    }

    @Test
    fun secretariaNaoVePedidoDeOutroCursoEProfessorContinuaVendoFila() {
        val tokenAluno = login(EMAIL_ALUNO_OUTRO)
        val created = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $tokenAluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadNova()),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val id = extract(created.response.contentAsString, "\"id\":\"", "\"")

        val tokenSec = login(EMAIL_SEC)
        mockMvc.perform(
            get("/requests")
                .param("canDeliberate", "true")
                .header("Authorization", "Bearer $tokenSec"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", not(hasItem(id))))

        mockMvc.perform(get("/requests/$id").header("Authorization", "Bearer $tokenSec"))
            .andExpect(status().isNotFound)

        val tokenProf = login(EMAIL_PROF)
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

    private fun criarUsuario(email: String, grr: String, authorities: List<String>, agora: OffsetDateTime): Usuario {
        val existente = usuarioRepository.findByEmail(email)
        if (existente.isPresent) {
            return existente.get()
        }
        return usuarioRepository.save(
            Usuario(
                Uuids.v7(),
                Email.of(email),
                null,
                Grr.of(grr),
                passwordHasher.hash(SENHA),
                true,
                agora,
                "127.0.0.1",
                "it",
                true,
                0,
                null,
                authorities,
                agora,
                agora,
            ),
        )
    }

    private fun login(email: String): String {
        val result = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificador\":\"$email\",\"senha\":\"$SENHA\"}"),
        )
            .andExpect(status().isOk)
            .andReturn()
        return extract(result.response.contentAsString, "\"accessToken\":\"", "\"")
    }

    companion object {
        private const val SENHA = "TroqueEstaSenha1!"
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

        private fun extract(json: String, startToken: String, endToken: String): String {
            val start = json.indexOf(startToken)
            val from = start + startToken.length
            val end = json.indexOf(endToken, from)
            return json.substring(from, end)
        }
    }
}
