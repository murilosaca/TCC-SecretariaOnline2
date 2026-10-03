package br.ufpr.sept.so2.modules.comunicacao.api

import br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence.TemplateComunicacaoJpaRepository
import br.ufpr.sept.so2.modules.comunicacao.infrastructure.persistence.TemplateRevisaoJpaRepository
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TemplateComunicacaoIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var cabecalhos: TemplateComunicacaoJpaRepository

    @Autowired
    private lateinit var revisoes: TemplateRevisaoJpaRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        usuariosIt.criarUsuario(EMAIL_ADMIN, "GRR20243401", listOf("communication.manage_templates"))
        usuariosIt.criarUsuario(EMAIL_SEC, "GRR20243402", listOf("course.manage"))
    }

    @Test
    fun anonimoRecebe401ESemCapRecebe403() {
        mockMvc.perform(get("/admin/templates-comunicacao"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.type").value(containsString("authentication-required")))
        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(get("/admin/templates-comunicacao").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.type").value(containsString("access-denied")))
    }

    @Test
    fun nomeDuplicadoNaoGravaERevisaoNovaArquivaAAnterior() {
        val admin = usuariosIt.login(EMAIL_ADMIN)
        val nome = "boas-vindas.fatia34"
        val criado = mockMvc.perform(
            post("/admin/templates-comunicacao")
                .header("Authorization", "Bearer $admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(nome, "Boas-vindas", "Olá {{nome}}")),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.revisoes[0].status").value("CURRENT"))
            .andExpect(jsonPath("$.variaveis[0]").value("nome"))
            .andReturn()
            .response
            .contentAsString
        val id = UUID.fromString(objectMapper.readTree(criado)["id"].asText())

        mockMvc.perform(
            post("/admin/templates-comunicacao")
                .header("Authorization", "Bearer $admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(nome, "Outro", "corpo")),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.type").value(containsString("conflict")))

        org.junit.jupiter.api.Assertions.assertEquals(1, revisoes.findByTemplateIdOrderByVersaoDesc(id).size)
        org.junit.jupiter.api.Assertions.assertTrue(cabecalhos.findByNome(nome).isPresent)

        mockMvc.perform(
            post("/admin/templates-comunicacao/$id/revisoes")
                .header("Authorization", "Bearer $admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(null, "Boas-vindas 2", "Olá {{nome}}, protocolo {{protocolo}}")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.revisoes[0].status").value("CURRENT"))
            .andExpect(jsonPath("$.revisoes[0].versao").value(2))
            .andExpect(jsonPath("$.revisoes[1].status").value("ARCHIVED"))
            .andExpect(jsonPath("$.revisoes[1].versao").value(1))

        val gravadas = revisoes.findByTemplateIdOrderByVersaoDesc(id)
        org.junit.jupiter.api.Assertions.assertEquals(1, gravadas.count { it.status == "CURRENT" })
        org.junit.jupiter.api.Assertions.assertEquals(1, gravadas.count { it.status == "ARCHIVED" })
    }

    private fun corpo(nome: String?, assunto: String, texto: String): String =
        objectMapper.writeValueAsString(mapOf("nome" to nome, "assunto" to assunto, "corpo" to texto))

    companion object {
        private const val EMAIL_ADMIN = "admin.tpl34@ufpr.br"
        private const val EMAIL_SEC = "sec.tpl34@ufpr.br"
    }
}
