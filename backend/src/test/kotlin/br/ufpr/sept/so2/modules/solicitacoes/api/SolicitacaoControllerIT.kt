package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
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
class SolicitacaoControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var tipoSolicitacaoRepository: TipoSolicitacaoRepository

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        if (tipoSolicitacaoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty) {
            tipoSolicitacaoRepository.save(DeclaracaoSimplesSeed.tipo(agora))
        }
        if (usuarioRepository.findByEmail("it.solicitacao@ufpr.br").isEmpty) {
            usuarioRepository.save(
                Usuario(
                    Uuids.v7(),
                    Email.of("it.solicitacao@ufpr.br"),
                    null,
                    Grr.of("GRR20248888"),
                    passwordHasher.hash("TroqueEstaSenha1!"),
                    true,
                    agora,
                    "127.0.0.1",
                    "it",
                    true,
                    0,
                    null,
                    listOf("dashboard.view_own", "request.view_own", "request.open"),
                    agora,
                    agora,
                ),
            )
        }
        if (usuarioRepository.findByEmail("it.outro@ufpr.br").isEmpty) {
            usuarioRepository.save(
                Usuario(
                    Uuids.v7(),
                    Email.of("it.outro@ufpr.br"),
                    null,
                    Grr.of("GRR20247777"),
                    passwordHasher.hash("TroqueEstaSenha1!"),
                    true,
                    agora,
                    "127.0.0.1",
                    "it",
                    true,
                    0,
                    null,
                    listOf("dashboard.view_own", "request.view_own", "request.open"),
                    agora,
                    agora,
                ),
            )
        }
    }

    @Test
    fun criarListarEObterRequerAutenticacao() {
        mockMvc.perform(get("/requests"))
            .andExpect(status().isUnauthorized)

        val token = login("it.solicitacao@ufpr.br")

        mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"tipoCodigo":"DECLARACAO_SIMPLES","payload":{"finalidade":"abc"}}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isUnprocessableEntity)

        val created = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "tipoCodigo": "DECLARACAO_SIMPLES",
                      "payload": {
                        "finalidade": "Comprovação de vínculo",
                        "observacao": "Para estágio"
                      }
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.protocolo", startsWith("PROT-")))
            .andExpect(jsonPath("$.estado").value("EM_ANALISE"))
            .andExpect(jsonPath("$.tipoCodigo").value("DECLARACAO_SIMPLES"))
            .andExpect(jsonPath("$._links.self").exists())
            .andExpect(jsonPath("$._links.deliberar").doesNotExist())
            .andExpect(jsonPath("$.eventos[0].tipo").value("CRIADA"))
            .andReturn()

        val id = extract(created.response.contentAsString, "\"id\":\"", "\"")

        mockMvc.perform(
            get("/requests?solicitante=me")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(id))
            .andExpect(jsonPath("$._links.novaSolicitacao").value("/request-types"))

        mockMvc.perform(
            get("/requests/$id")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.eventos[0].tipo").value("CRIADA"))

        val outro = login("it.outro@ufpr.br")
        mockMvc.perform(
            get("/requests/$id")
                .header("Authorization", "Bearer $outro"),
        )
            .andExpect(status().isNotFound)
    }

    private fun login(email: String): String {
        val result = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificador\":\"$email\",\"senha\":\"TroqueEstaSenha1!\"}"),
        )
            .andExpect(status().isOk)
            .andReturn()
        return extract(result.response.contentAsString, "\"accessToken\":\"", "\"")
    }

    companion object {
        private fun extract(json: String, startToken: String, endToken: String): String {
            val start = json.indexOf(startToken)
            val from = start + startToken.length
            val end = json.indexOf(endToken, from)
            return json.substring(from, end)
        }
    }
}
