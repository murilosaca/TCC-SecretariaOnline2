package br.ufpr.sept.so2.modules.solicitacoes.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TipoSolicitacaoEditorIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var tipoSolicitacaoRepository: TipoSolicitacaoRepository

    @Autowired
    private lateinit var solicitacaoRepository: SolicitacaoRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        usuariosIt.criarUsuario(EMAIL_ADMIN, "GRR20243301", listOf("request_type.manage"))
        usuariosIt.criarUsuario(EMAIL_ALUNO, "GRR20243302", listOf("request.open", "request.view_own"))
        usuariosIt.criarUsuario(EMAIL_SEC, "GRR20243303", listOf("course.manage"))
    }

    @Test
    fun anonimoRecebe401ESemCapRecebe403() {
        mockMvc.perform(get("/admin/tipos-solicitacao"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.type").value(containsString("authentication-required")))
        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(get("/admin/tipos-solicitacao").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.type").value(containsString("access-denied")))
    }

    @Test
    fun publicarInvalidoNaoAvancaVersaoESolicitacaoAbertaMantemSnapshot() {
        val admin = usuariosIt.login(EMAIL_ADMIN)
        val aluno = usuariosIt.login(EMAIL_ALUNO)
        val codigo = "TIPO_FATIA33"
        val criado = mockMvc.perform(
            post("/admin/tipos-solicitacao")
                .header("Authorization", "Bearer $admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(codigo, "Tipo fatia", FORM_V1, FLUXO)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andReturn()
            .response
            .contentAsString
        val tipoId = objectMapper.readTree(criado)["id"].asText()

        mockMvc.perform(get("/request-types").header("Authorization", "Bearer $aluno"))
            .andExpect(status().isOk)
            .andExpect(content().string(not(containsString(codigo))))

        mockMvc.perform(
            post("/admin/tipos-solicitacao/$tipoId/publicar")
                .header("Authorization", "Bearer $admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(null, null, FORM_V1, FLUXO)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PUBLISHED"))
            .andExpect(jsonPath("$.versao").value(1))

        val publicado = tipoSolicitacaoRepository.findById(UUID.fromString(tipoId)).orElseThrow()
        assertEquals(1, publicado.versao)
        assertEquals(FORM_V1, publicado.formSchema)

        mockMvc.perform(
            post("/admin/tipos-solicitacao/$tipoId/publicar")
                .header("Authorization", "Bearer $admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(null, null, "{ ", FLUXO)),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value(containsString("invalid-schema")))

        val aposInvalido = tipoSolicitacaoRepository.findById(UUID.fromString(tipoId)).orElseThrow()
        assertEquals(1, aposInvalido.versao)
        assertEquals(FORM_V1, aposInvalido.formSchema)

        val aberta = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $aluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"tipoCodigo":"$codigo","payload":{"motivo":"texto"}}"""),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.tipoVersao").value(1))
            .andReturn()
            .response
            .contentAsString
        val solicitacaoId = objectMapper.readTree(aberta)["id"].asText()

        mockMvc.perform(
            post("/admin/tipos-solicitacao/$tipoId/publicar")
                .header("Authorization", "Bearer $admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(null, null, FORM_V2, FLUXO)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.versao").value(2))

        val depois = tipoSolicitacaoRepository.findById(UUID.fromString(tipoId)).orElseThrow()
        assertEquals(FORM_V2, depois.formSchema)
        val solicitacao = solicitacaoRepository.findById(UUID.fromString(solicitacaoId)).orElseThrow()
        assertEquals(FORM_V1, solicitacao.formSchemaSnapshot)
        assertEquals(1, solicitacao.tipoVersao)
    }

    private fun corpo(codigo: String?, nome: String?, form: String, fluxo: String): String =
        objectMapper.writeValueAsString(
            mapOf(
                "codigo" to codigo,
                "nome" to nome,
                "prazoDias" to 5,
                "formSchema" to form,
                "workflowJson" to fluxo,
            ),
        )

    companion object {
        private const val EMAIL_ADMIN = "tipos.admin.it@ufpr.br"
        private const val EMAIL_ALUNO = "tipos.aluno.it@ufpr.br"
        private const val EMAIL_SEC = "tipos.sec.it@ufpr.br"
        private const val FORM_V1 =
            """{"type":"object","properties":{"motivo":{"type":"string","title":"Motivo"}},"required":["motivo"]}"""
        private const val FORM_V2 =
            """{"type":"object","properties":{"motivo":{"type":"string","title":"Motivo novo"},"extra":{"type":"string","title":"Extra"}},"required":["motivo"]}"""
        private const val FLUXO =
            """{"initial":"EM_ANALISE","states":{"EM_ANALISE":{"on":{"DEFER":"DELIBERADA"}},"DELIBERADA":{"on":{}}}}"""
    }
}
