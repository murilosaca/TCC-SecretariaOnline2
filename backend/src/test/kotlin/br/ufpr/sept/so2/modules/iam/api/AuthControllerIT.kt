package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    lateinit var passwordHasher: PasswordHasher

    @BeforeEach
    fun seed() {
        if (usuarioRepository.findByEmail("it.login@ufpr.br").isPresent) {
            return
        }
        val agora = OffsetDateTime.now()
        usuarioRepository.save(
            Usuario(
                Uuids.v7(),
                Email.of("it.login@ufpr.br"),
                Email.of("pessoal.it@gmail.com"),
                Grr.of("GRR20249999"),
                passwordHasher.hash("TroqueEstaSenha1!"),
                true,
                agora,
                "127.0.0.1",
                "it",
                true,
                0,
                null,
                listOf("dashboard.view_own"),
                agora,
                agora,
            ),
        )
    }

    @Test
    fun loginSucessoComEmailEGrr() {
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"identificador":"it.login@ufpr.br","senha":"TroqueEstaSenha1!"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.mustChangePassword").value(false))
            .andExpect(jsonPath("$.expiresIn").value(900))
            .andExpect(jsonPath("$.refreshToken").doesNotExist())
            .andExpect(cookie().exists("so2_refresh"))
            .andExpect(cookie().httpOnly("so2_refresh", true))

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"identificador":"GRR20249999","senha":"TroqueEstaSenha1!"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
    }

    @Test
    fun loginFalhaAntiEnumeracao() {
        val inexistente = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"identificador":"sumido@ufpr.br","senha":"qualquer"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(
                jsonPath("$.detail").value(
                    "Credenciais inválidas. Verifique seus dados e tente novamente.",
                ),
            )
            .andReturn()
            .response
            .contentAsString

        val senhaErrada = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"identificador":"it.login@ufpr.br","senha":"errada"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(
                jsonPath("$.detail").value(
                    "Credenciais inválidas. Verifique seus dados e tente novamente.",
                ),
            )
            .andReturn()
            .response
            .contentAsString

        assertEquals(extractDetail(inexistente), extractDetail(senhaErrada))
    }

    @Test
    fun loginNativoDevolveRefreshNoJsonSemOmitirCookie() {
        mockMvc.perform(
            post("/auth/login")
                .header(NativeClient.HEADER, NativeClient.VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"identificador":"it.login@ufpr.br","senha":"TroqueEstaSenha1!"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty)
            .andExpect(cookie().exists("so2_refresh"))
    }

    @Test
    fun refreshPorCookieWebNaoExpoeRefreshNoJson() {
        val login = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"identificador":"it.login@ufpr.br","senha":"TroqueEstaSenha1!"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andReturn()
        val cookie = login.response.getCookie("so2_refresh")
        assertNotNull(cookie)

        mockMvc.perform(post("/auth/refresh").cookie(cookie))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").doesNotExist())
            .andExpect(cookie().exists("so2_refresh"))
    }

    @Test
    fun refreshPorBodyNativoRotacionaERejeitaOAntigo() {
        val login = mockMvc.perform(
            post("/auth/login")
                .header(NativeClient.HEADER, NativeClient.VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"identificador":"it.login@ufpr.br","senha":"TroqueEstaSenha1!"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andReturn()
        val refreshToken = JsonPath.read<String>(login.response.contentAsString, "$.refreshToken")

        mockMvc.perform(
            post("/auth/refresh")
                .header(NativeClient.HEADER, NativeClient.VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"$refreshToken"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty)

        mockMvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"$refreshToken"}"""),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun logoutPorBodyRevogaRefreshNativo() {
        val login = mockMvc.perform(
            post("/auth/login")
                .header(NativeClient.HEADER, NativeClient.VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"identificador":"it.login@ufpr.br","senha":"TroqueEstaSenha1!"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andReturn()
        val json = login.response.contentAsString
        val accessToken = JsonPath.read<String>(json, "$.accessToken")
        val refreshToken = JsonPath.read<String>(json, "$.refreshToken")

        mockMvc.perform(
            post("/auth/logout")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"$refreshToken"}"""),
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"$refreshToken"}"""),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun recuperarSenhaSempreAccepted() {
        mockMvc.perform(
            post("/auth/recuperar-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"it.login@ufpr.br"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isAccepted)
        mockMvc.perform(
            post("/auth/recuperar-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"naoexiste@ufpr.br"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isAccepted)
    }

    companion object {
        private fun extractDetail(json: String): String {
            val prefix = "\"detail\":\""
            val start = json.indexOf(prefix) + prefix.length
            val end = json.indexOf('"', start)
            return json.substring(start, end)
        }
    }
}
