package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeliberacaoSolicitacaoIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var tipoSolicitacaoRepository: TipoSolicitacaoRepository

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        if (tipoSolicitacaoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty) {
            tipoSolicitacaoRepository.save(DeclaracaoSimplesSeed.tipo(agora))
        }
        usuariosIt.criarUsuario(
            EMAIL_ALUNO,
            "GRR20248811",
            listOf("dashboard.view_own", "request.view_own", "request.open"),
        )
        usuariosIt.criarUsuario(
            EMAIL_PROFESSOR,
            "GRR20248812",
            listOf("dashboard.view_own", "event.manage", "event.host", "request.deliberate"),
        )
        usuariosIt.criarUsuario(
            EMAIL_OUTRO,
            "GRR20248813",
            listOf("dashboard.view_own", "request.view_own", "request.open"),
        )
    }

    @Test
    fun professorDeferePontaAPontaEAlunoNaoDelibera() {
        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)
        val created = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $tokenAluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadNova()),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.estado").value("EM_ANALISE"))
            .andExpect(jsonPath("$._links.deferir").doesNotExist())
            .andExpect(jsonPath("$._links.deliberar").doesNotExist())
            .andReturn()
        val id = extract(created.response.contentAsString, "\"id\":\"", "\"")

        mockMvc.perform(
            post("/requests/$id/transitions")
                .header("Authorization", "Bearer $tokenAluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"action":"DEFER","parecer":"Não deveria passar."}"""),
        )
            .andExpect(status().isForbidden)

        val tokenProfessor = usuariosIt.login(EMAIL_PROFESSOR)
        mockMvc.perform(get("/requests").header("Authorization", "Bearer $tokenProfessor"))
            .andExpect(status().isForbidden)

        mockMvc.perform(
            get("/requests")
                .param("canDeliberate", "true")
                .header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", hasItem(id)))

        mockMvc.perform(
            get("/requests/$id")
                .header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.deliberar").value("/requests/$id"))
            .andExpect(jsonPath("$._links.deferir").value("/requests/$id/transitions"))
            .andExpect(jsonPath("$._links.indeferir").exists())
            .andExpect(jsonPath("$._links.solicitar-ajustes").exists())

        mockMvc.perform(
            post("/requests/$id/transitions")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"action":"INDEFER","parecer":"curto"}"""),
        )
            .andExpect(status().isUnprocessableEntity)

        mockMvc.perform(
            post("/requests/$id/transitions")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"action":"CLOSE","parecer":"Parecer suficiente para o teste."}"""),
        )
            .andExpect(status().isUnprocessableEntity)

        mockMvc.perform(
            post("/requests/$id/transitions")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"action":"DEFER","parecer":"Deferido para comprovação de vínculo."}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("DELIBERADA"))
            .andExpect(jsonPath("$._links.deferir").doesNotExist())
            .andExpect(jsonPath("$._links.deliberar").doesNotExist())
            .andExpect(jsonPath("$.eventos[0].tipo").value("TRANSICAO"))
            .andExpect(jsonPath("$.eventos[0].parecer", startsWith("Deferido")))

        mockMvc.perform(
            get("/requests")
                .param("canDeliberate", "true")
                .header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", not(hasItem(id))))

        mockMvc.perform(
            get("/requests/$id")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("DELIBERADA"))

        val tokenOutro = usuariosIt.login(EMAIL_OUTRO)
        mockMvc.perform(
            get("/requests/$id")
                .header("Authorization", "Bearer $tokenOutro"),
        )
            .andExpect(status().isNotFound)
    }

    companion object {
        private const val EMAIL_ALUNO = "it.deliberacao@ufpr.br"
        private const val EMAIL_PROFESSOR = "it.deliberacao.prof@ufpr.br"
        private const val EMAIL_OUTRO = "it.deliberacao.outro@ufpr.br"

        private fun payloadNova(): String =
            """
            {
              "tipoCodigo": "DECLARACAO_SIMPLES",
              "payload": {
                "finalidade": "Comprovação de vínculo",
                "observacao": "Banca de deliberação"
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
