package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import org.hamcrest.Matchers.containsString
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUsuarioControllerIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        usuariosIt.criarUsuario(
            EMAIL_ADMIN,
            "GRR20247901",
            listOf("dashboard.view_own", "user.manage_all", "user.reset_password"),
        )
        usuariosIt.criarUsuario(
            EMAIL_SEC,
            "GRR20247902",
            listOf(
                "course.manage",
                "subject.manage",
                "user.manage_students",
                "calendar.manage",
            ),
        )
        usuariosIt.criarUsuario(
            EMAIL_ALUNO,
            "GRR20247903",
            listOf("dashboard.view_own", "request.view_own"),
        )
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/admin/usuarios"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.type").value(containsString("authentication-required")))
    }

    @Test
    fun secretariaRecebe403NoAdmin() {
        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(get("/admin/usuarios").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.type").value(containsString("access-denied")))
    }

    @Test
    fun alunoRecebe403NoAdmin() {
        val token = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(get("/admin/usuarios").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun adminListaCriaEditaDesativaEReset() {
        val token = usuariosIt.login(EMAIL_ADMIN)
        mockMvc.perform(get("/admin/usuarios").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.criar").value("/admin/usuarios"))
            .andExpect(jsonPath("$.content").isArray)

        val criado = mockMvc.perform(
            post("/admin/usuarios")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Novo Admin","emailInstitucional":"novo.admin@ufpr.br","grr":"GRR20247999"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.nome").value("Novo Admin"))
            .andExpect(jsonPath("$.senhaAlterada").value(false))
            .andExpect(jsonPath("$.ativo").value(true))
            .andExpect(jsonPath("$._links.desativar").exists())
            .andExpect(jsonPath("$._links['reset-senha']").exists())
            .andExpect(jsonPath("$.senha").doesNotExist())
            .andExpect(jsonPath("$.senhaHash").doesNotExist())
            .andReturn()

        val body = criado.response.contentAsString
        val usuarioId = Regex("\"id\"\\s*:\\s*\"([^\"]+)\"").find(body)!!.groupValues[1]

        mockMvc.perform(
            put("/admin/usuarios/$usuarioId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nome":"Novo Admin Editado","emailInstitucional":"novo.admin@ufpr.br","grr":"GRR20247999"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nome").value("Novo Admin Editado"))

        mockMvc.perform(
            post("/admin/usuarios/$usuarioId/reset-senha")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isAccepted)

        mockMvc.perform(
            post("/admin/usuarios/$usuarioId/desativar")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ativo").value(false))
            .andExpect(jsonPath("$._links.desativar").doesNotExist())
            .andExpect(jsonPath("$._links['reset-senha']").doesNotExist())
    }

    @Test
    fun secretariaPodeBuscarNoPickerMasNaoNoAdmin() {
        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(get("/iam/usuarios").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
        mockMvc.perform(get("/admin/usuarios").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun meAdminTemLinkUsuarios() {
        val token = usuariosIt.login(EMAIL_ADMIN)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.usuarios").value("/admin/usuarios"))
    }

    companion object {
        private const val EMAIL_ADMIN = "it.admin.usuarios@ufpr.br"
        private const val EMAIL_SEC = "it.sec.usuarios@ufpr.br"
        private const val EMAIL_ALUNO = "it.aluno.usuarios@ufpr.br"
    }
}
