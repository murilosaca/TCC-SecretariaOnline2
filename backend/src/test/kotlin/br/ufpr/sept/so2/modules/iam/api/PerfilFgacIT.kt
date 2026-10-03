package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PerfilFgacIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        usuariosIt.criarUsuario(EMAIL_ADMIN, "GRR20243201", listOf("iam.manage_roles"))
        usuariosIt.criarUsuario(EMAIL_SEC, "GRR20243202", listOf("course.manage"))
    }

    @Test
    fun anonimoRecebe401ESemCapRecebe403() {
        mockMvc.perform(get("/admin/perfis"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.type").value(containsString("authentication-required")))
        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(get("/admin/perfis").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.type").value(containsString("access-denied")))
    }

    @Test
    fun perfilDeSistemaNaoTemExcluir() {
        val token = usuariosIt.login(EMAIL_ADMIN)
        val body = mockMvc.perform(get("/admin/perfis").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
        val aluno = objectMapper.readTree(body)["content"].first { it["nome"].asText() == "ALUNO" }
        assertEquals("SYSTEM", aluno["tipo"].asText())
        assertFalse(aluno["_links"].has("excluir"))
    }

    @Test
    fun excluirPerfilComUsuarioAtivoDevolve422ENaoApaga() {
        val token = usuariosIt.login(EMAIL_ADMIN)
        val titular = usuariosIt.criarUsuario(EMAIL_TITULAR, "GRR20243203", listOf("dashboard.view_own"))
        val criado = mockMvc.perform(
            post("/admin/perfis")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nome":"perfil_it_uso","descricao":"Uso","authorities":[]}"""),
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString
        val perfilId = objectMapper.readTree(criado)["id"].asText()

        mockMvc.perform(
            put("/admin/usuarios/${titular.id}/perfis")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"perfilIds":["$perfilId"]}"""),
        ).andExpect(status().isOk)

        mockMvc.perform(delete("/admin/perfis/$perfilId").header("Authorization", "Bearer $token"))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value(containsString("role-in-use")))

        mockMvc.perform(get("/admin/perfis/$perfilId").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nome").value("perfil_it_uso"))
    }

    companion object {
        private const val EMAIL_ADMIN = "perfis.admin.it@ufpr.br"
        private const val EMAIL_SEC = "perfis.sec.it@ufpr.br"
        private const val EMAIL_TITULAR = "perfis.titular.it@ufpr.br"
    }
}
