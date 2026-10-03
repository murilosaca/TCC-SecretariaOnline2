package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.persistence.RefreshTokenJpaRepository
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Base64
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PerfilAutogestaoIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var refreshTokenJpaRepository: RefreshTokenJpaRepository

    private val mapper = ObjectMapper()

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @Test
    fun patchNaoAlteraGrrNemEmailInstitucional() {
        val email = "it.perfil.patch@ufpr.br"
        usuariosIt.criarUsuario(email, "GRR20248111", CAP)
        val token = loginNativo(email).accessToken
        mockMvc.perform(
            patch("/me")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"telefone":"41999998888","nomeSocial":"Ana","grr":"GRR00000000","emailInstitucional":"outro@ufpr.br"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.telefone").value("41999998888"))
            .andExpect(jsonPath("$.nomeSocial").value("Ana"))
            .andExpect(jsonPath("$.grr").value("GRR20248111"))
            .andExpect(jsonPath("$.emailInstitucional").value(email))

        val salvo = usuarioRepository.findByEmail(email).orElseThrow()
        assertEquals("GRR20248111", salvo.grr?.value)
        assertEquals(email, salvo.emailInstitucional.value)
        assertEquals("Ana", salvo.nomeSocial)

        mockMvc.perform(
            patch("/me")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"telefone":"4133332222"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.telefone").value("4133332222"))
            .andExpect(jsonPath("$.nomeSocial").value("Ana"))
            .andExpect(jsonPath("$.grr").value("GRR20248111"))
    }

    @Test
    fun senhaAtualErradaNaoMudaHashNemSessao() {
        val email = "it.perfil.errada@ufpr.br"
        usuariosIt.criarUsuario(email, "GRR20248112", CAP)
        val antes = usuarioRepository.findByEmail(email).orElseThrow().senhaHash
        val login = loginNativo(email)
        mockMvc.perform(
            patch("/me/password")
                .header("Authorization", "Bearer ${login.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"senhaAtual":"nao-e-essa","novaSenha":"OutraSenhaForte1!"}"""),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.detail").value("Senha atual incorreta."))
            .andExpect(jsonPath("$.type").value("https://secretariaonline.ufpr.br/errors/senha-atual-incorreta"))

        assertEquals(antes, usuarioRepository.findByEmail(email).orElseThrow().senhaHash)
        loginNativo(email)
        mockMvc.perform(
            post("/auth/refresh")
                .header(NativeClient.HEADER, NativeClient.VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"${login.refreshToken}"}"""),
        ).andExpect(status().isOk)
    }

    @Test
    fun trocaValidaDerrubaOutroRefreshEMantemAtual() {
        val email = "it.perfil.troca@ufpr.br"
        usuariosIt.criarUsuario(email, "GRR20248113", CAP)
        val atual = loginNativo(email, "Mozilla/5.0 (Windows NT 10.0) Chrome/120.0")
        val outra = loginNativo(email, "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) Safari/604.1")

        mockMvc.perform(
            patch("/me/password")
                .header("Authorization", "Bearer ${atual.accessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"senhaAtual":"${ItUsuarioFixture.SENHA}","novaSenha":"OutraSenhaForte1!"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mensagem").value("Senha alterada. Outras sessões foram encerradas."))

        mockMvc.perform(
            post("/auth/refresh")
                .header(NativeClient.HEADER, NativeClient.VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"${outra.refreshToken}"}"""),
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            post("/auth/refresh")
                .header(NativeClient.HEADER, NativeClient.VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"${atual.refreshToken}"}"""),
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"identificador":"$email","senha":"${ItUsuarioFixture.SENHA}"}"""),
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"identificador":"$email","senha":"OutraSenhaForte1!"}"""),
        ).andExpect(status().isOk)
    }

    @Test
    fun encerrarSessaoAtualOuAlheiaSemLinkNaoOcorre() {
        val email = "it.perfil.sessoes@ufpr.br"
        val outroEmail = "it.perfil.alheio@ufpr.br"
        usuariosIt.criarUsuario(email, "GRR20248114", CAP)
        usuariosIt.criarUsuario(outroEmail, "GRR20248115", CAP)
        val atual = loginNativo(email, "Mozilla/5.0 (Windows NT 10.0) Chrome/120.0")
        val outra = loginNativo(email, "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) Safari/604.1")
        val alheia = loginNativo(outroEmail)

        val corpo = mockMvc.perform(get("/me/sessions").header("Authorization", "Bearer ${atual.accessToken}"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
        val itens = nos(corpo)
        val corrente = itens.first { it.get("atual").asBoolean() }
        val demais = itens.first { !it.get("atual").asBoolean() }
        assertTrue(corrente.get("_links") == null || !corrente.get("_links").has("encerrar"))
        assertEquals("iPhone · Safari", demais.get("dispositivo").asText())
        val href = demais.get("_links").get("encerrar").asText()
        assertTrue(href.endsWith("/me/sessions/${demais.get("id").asText()}"))

        mockMvc.perform(
            delete("/me/sessions/${corrente.get("id").asText()}")
                .header("Authorization", "Bearer ${atual.accessToken}"),
        ).andExpect(status().isConflict)
        assertFalse(sessao(corrente).revoked)

        val idAlheia = idAtual(alheia.accessToken)
        mockMvc.perform(
            delete("/me/sessions/$idAlheia").header("Authorization", "Bearer ${atual.accessToken}"),
        ).andExpect(status().isNotFound)
        assertFalse(refreshTokenJpaRepository.findById(UUID.fromString(idAlheia)).orElseThrow().revoked)

        mockMvc.perform(
            delete(href).header("Authorization", "Bearer ${atual.accessToken}"),
        ).andExpect(status().isNoContent)
        assertTrue(sessao(demais).revoked)
        assertFalse(sessao(corrente).revoked)

        mockMvc.perform(
            post("/auth/refresh")
                .header(NativeClient.HEADER, NativeClient.VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"${outra.refreshToken}"}"""),
        ).andExpect(status().isUnauthorized)
        mockMvc.perform(
            post("/auth/refresh")
                .header(NativeClient.HEADER, NativeClient.VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"${atual.refreshToken}"}"""),
        ).andExpect(status().isOk)
        mockMvc.perform(
            post("/auth/refresh")
                .header(NativeClient.HEADER, NativeClient.VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"${alheia.refreshToken}"}"""),
        ).andExpect(status().isOk)
    }

    @Test
    fun semCapabilityEAnonimoNaoEntramECriticalNaoDesliga() {
        mockMvc.perform(get("/me")).andExpect(status().isUnauthorized)
        val semCap = "it.perfil.semcap@ufpr.br"
        usuariosIt.criarUsuario(semCap, "GRR20248116", listOf("dashboard.view_own"))
        val tokenSem = usuariosIt.login(semCap)
        mockMvc.perform(get("/me").header("Authorization", "Bearer $tokenSem"))
            .andExpect(status().isForbidden)

        val email = "it.perfil.notif@ufpr.br"
        usuariosIt.criarUsuario(email, "GRR20248117", CAP)
        val token = loginNativo(email).accessToken
        mockMvc.perform(
            patch("/me/notifications")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"canais":{"CRITICAL":{"email":false}}}"""),
        ).andExpect(status().isUnprocessableEntity)

        mockMvc.perform(
            patch("/me/notifications")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"canais":{"MEDIUM":{"email":false}},"dndInicio":"22:00","dndFim":"07:00","digest":"RESUMO"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.canais.CRITICAL.email").value(true))
            .andExpect(jsonPath("$.canais.MEDIUM.email").value(false))
            .andExpect(jsonPath("$.digest").value("RESUMO"))
            .andExpect(jsonPath("$.dndInicio").value("22:00"))

        val png = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
        )
        mockMvc.perform(
            multipart("/me/foto")
                .file(MockMultipartFile("arquivo", "foto.png", "image/png", png))
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.fotoUrl").isNotEmpty)
            .andExpect(jsonPath("$.grr").value("GRR20248117"))
        assertNotNull(usuarioRepository.findByEmail(email).orElseThrow().fotoStorageKey)
    }

    private fun loginNativo(email: String, userAgent: String = "it"): LoginNativo {
        val result = mockMvc.perform(
            post("/auth/login")
                .header(NativeClient.HEADER, NativeClient.VALUE)
                .header("User-Agent", userAgent)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"identificador":"$email","senha":"${ItUsuarioFixture.SENHA}"}"""),
        )
            .andExpect(status().isOk)
            .andReturn()
        val json = result.response.contentAsString
        return LoginNativo(ItJson.text(json, "accessToken"), ItJson.text(json, "refreshToken"))
    }

    private fun idAtual(accessToken: String): String {
        val corpo = mockMvc.perform(get("/me/sessions").header("Authorization", "Bearer $accessToken"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
        return nos(corpo).first { it.get("atual").asBoolean() }.get("id").asText()
    }

    private fun nos(json: String): List<JsonNode> {
        val itens = mapper.readTree(json).get("itens")
        return (0 until itens.size()).map { itens.get(it) }
    }

    private fun sessao(item: JsonNode) =
        refreshTokenJpaRepository.findById(UUID.fromString(item.get("id").asText())).orElseThrow()

    private data class LoginNativo(val accessToken: String, val refreshToken: String)

    companion object {
        private val CAP = listOf("user.update_own_profile", "dashboard.view_own")
    }
}
