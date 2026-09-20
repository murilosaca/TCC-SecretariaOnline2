package br.ufpr.sept.so2.modules.academico.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CursoControllerIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        criarUsuario(EMAIL_ALUNO, "GRR20247101", AUTHORITIES_ALUNO, agora)
        criarUsuario(EMAIL_SEC, "GRR20247102", AUTHORITIES_SEC, agora)
        criarUsuario(EMAIL_SEC_B, "GRR20247103", AUTHORITIES_SEC, agora)
    }

    @Test
    fun semBearerRetorna401() {
        mockMvc.perform(get("/academico/cursos"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.type").value(org.hamcrest.Matchers.containsString("authentication-required")))
    }

    @Test
    fun alunoRecebe403NoCrud() {
        val token = login(EMAIL_ALUNO)
        mockMvc.perform(get("/academico/cursos").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.type").value(org.hamcrest.Matchers.containsString("access-denied")))
        mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("TADS-ALN", "TADSA", "TADS-ALN-1", emptyList())),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun secretariaCriaListaEPersisteSecretarios() {
        val token = login(EMAIL_SEC)
        val secretariaId = usuarioRepository.findByEmail(EMAIL_SEC).get().id
        val created = mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("TADS FGAC", "TADSF", "TADS-FGAC-1", listOf(secretariaId.toString()))),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.sigla").value("TADSF"))
            .andExpect(jsonPath("$.secretariosIds[0]").value(secretariaId.toString()))
            .andExpect(jsonPath("$._links.atualizar").exists())
            .andExpect(jsonPath("$._links.criar").doesNotExist())
            .andReturn()
        val id = extract(created.response.contentAsString, "\"id\":\"", "\"")

        mockMvc.perform(get("/academico/cursos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.criar").value("/academico/cursos"))
            .andExpect(jsonPath("$.content[*].id").value(org.hamcrest.Matchers.hasItem(id)))

        mockMvc.perform(get("/academico/cursos/$id").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.secretariosIds[0]").value(secretariaId.toString()))
    }

    @Test
    fun cursoDeOutroSecretarioRetorna404() {
        val tokenA = login(EMAIL_SEC)
        val tokenB = login(EMAIL_SEC_B)
        val idA = usuarioRepository.findByEmail(EMAIL_SEC).get().id
        val idB = usuarioRepository.findByEmail(EMAIL_SEC_B).get().id
        val createdB = mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $tokenB")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("Outro curso", "OUTRO", "OUTRO-FGAC-1", listOf(idB.toString()))),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val idFora = extract(createdB.response.contentAsString, "\"id\":\"", "\"")

        mockMvc.perform(get("/academico/cursos/$idFora").header("Authorization", "Bearer $tokenA"))
            .andExpect(status().isNotFound)
        mockMvc.perform(get("/academico/cursos").header("Authorization", "Bearer $tokenA"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", not(org.hamcrest.Matchers.hasItem(idFora))))

        mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $tokenA")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("Curso A", "CURSA", "CURSA-FGAC-1", listOf(idA.toString()))),
        )
            .andExpect(status().isCreated)
    }

    private fun criarUsuario(email: String, grr: String, authorities: List<String>, agora: OffsetDateTime) {
        if (usuarioRepository.findByEmail(email).isPresent) {
            return
        }
        usuarioRepository.save(
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
        private const val EMAIL_ALUNO = "it.fgac.aluno@ufpr.br"
        private const val EMAIL_SEC = "it.fgac.sec@ufpr.br"
        private const val EMAIL_SEC_B = "it.fgac.secb@ufpr.br"
        private val AUTHORITIES_ALUNO = listOf("dashboard.view_own", "request.view_own", "request.open")
        private val AUTHORITIES_SEC = listOf(
            "course.manage",
            "subject.manage",
            "user.manage_students",
            "calendar.manage",
            "request.view_curso",
            "request.triage",
            "request.deliberate",
        )

        private fun payloadCurso(nome: String, sigla: String, codigo: String, secretarios: List<String>): String {
            val ids = secretarios.joinToString(",") { "\"$it\"" }
            return """
                {
                  "nome": "$nome",
                  "sigla": "$sigla",
                  "codigo": "$codigo",
                  "horasFormativasMinimas": 120,
                  "secretariosIds": [$ids]
                }
            """.trimIndent()
        }

        private fun extract(json: String, startToken: String, endToken: String): String {
            val start = json.indexOf(startToken)
            val from = start + startToken.length
            val end = json.indexOf(endToken, from)
            return json.substring(from, end)
        }
    }
}
