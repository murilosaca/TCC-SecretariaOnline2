package br.ufpr.sept.so2.modules.academico.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
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
        criarUsuario(EMAIL_COORD, "GRR20247104", listOf("course.manage"), agora)
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
        val id = ItJson.text(created.response.contentAsString, "id")

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
        val idFora = ItJson.text(createdB.response.contentAsString, "id")

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

    @Test
    fun criaSemSecretariosIncluiOCriadorEExpoeLinksDeEscopo() {
        val token = login(EMAIL_SEC)
        val secretariaId = usuarioRepository.findByEmail(EMAIL_SEC).get().id
        val (sigla, codigo) = codigoUnico("VZ")
        val created = mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("Curso sem lista", sigla, codigo, emptyList())),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.secretariosIds[0]").value(secretariaId.toString()))
            .andExpect(jsonPath("$._links.atualizar").exists())
            .andReturn()
        val id = ItJson.text(created.response.contentAsString, "id")

        mockMvc.perform(get("/academico/cursos/$id").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.atualizar").exists())
        mockMvc.perform(get("/academico/cursos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id").value(org.hamcrest.Matchers.hasItem(id)))
            .andExpect(jsonPath("$.content[?(@.id=='$id')]._links.atualizar").exists())
    }

    @Test
    fun putComSiglaDeOutroCursoDoEscopoRetorna409() {
        val token = login(EMAIL_SEC)
        val secretariaId = usuarioRepository.findByEmail(EMAIL_SEC).get().id.toString()
        val (siglaA, codigoA) = codigoUnico("DA")
        val (siglaB, codigoB) = codigoUnico("DB")
        val primeiro = mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("Curso Dup A", siglaA, codigoA, listOf(secretariaId))),
        )
            .andExpect(status().isCreated)
            .andReturn()
        mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("Curso Dup B", siglaB, codigoB, listOf(secretariaId))),
        )
            .andExpect(status().isCreated)
        val idA = ItJson.text(primeiro.response.contentAsString, "id")

        mockMvc.perform(
            put("/academico/cursos/$idA")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("Curso Dup A", siglaB, codigoA, listOf(secretariaId))),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.type").value(org.hamcrest.Matchers.containsString("conflict")))
    }

    @Test
    fun coordenadorSemLinhaDeSecretarioVeOCurso() {
        val tokenCoord = login(EMAIL_COORD)
        val tokenSec = login(EMAIL_SEC)
        val coordenadorId = usuarioRepository.findByEmail(EMAIL_COORD).get().id
        val (sigla, codigo) = codigoUnico("CO")
        val created = mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $tokenSec")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "nome": "Curso do coordenador",
                      "sigla": "$sigla",
                      "codigo": "$codigo",
                      "idCoordenador": "$coordenadorId",
                      "horasFormativasMinimas": 120,
                      "secretariosIds": []
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val id = ItJson.text(created.response.contentAsString, "id")

        mockMvc.perform(get("/academico/cursos").header("Authorization", "Bearer $tokenCoord"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id").value(hasItem(id)))
    }

    @Test
    fun coordenadorPutNaoGanhaLinhaDeSecretario() {
        val tokenCoord = login(EMAIL_COORD)
        val tokenSec = login(EMAIL_SEC)
        val coordenadorId = usuarioRepository.findByEmail(EMAIL_COORD).get().id
        val (sigla, codigo) = codigoUnico("CP")
        val created = mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $tokenSec")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "nome": "Curso PUT coordenador",
                      "sigla": "$sigla",
                      "codigo": "$codigo",
                      "idCoordenador": "$coordenadorId",
                      "horasFormativasMinimas": 120,
                      "secretariosIds": []
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val id = ItJson.text(created.response.contentAsString, "id")

        mockMvc.perform(
            put("/academico/cursos/$id")
                .header("Authorization", "Bearer $tokenCoord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("Curso PUT coordenador", sigla, codigo, emptyList())),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.secretariosIds", not(hasItem(coordenadorId.toString()))))
    }

    @Test
    fun secretarioPutSemOProprioIdNaoSofreLockout() {
        val token = login(EMAIL_SEC)
        val secretariaId = usuarioRepository.findByEmail(EMAIL_SEC).get().id
        val (sigla, codigo) = codigoUnico("SL")
        val created = mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("Curso anti-lockout", sigla, codigo, emptyList())),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val id = ItJson.text(created.response.contentAsString, "id")

        mockMvc.perform(
            put("/academico/cursos/$id")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("Curso anti-lockout", sigla, codigo, emptyList())),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.secretariosIds", hasItem(secretariaId.toString())))

        mockMvc.perform(get("/academico/cursos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", hasItem(id)))
    }

    @Test
    fun getCursoSegueLinkDisciplinasCom200() {
        val token = login(EMAIL_SEC)
        val (sigla, codigo) = codigoUnico("LD")
        val created = mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadCurso("Curso link disciplinas", sigla, codigo, emptyList())),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val id = ItJson.text(created.response.contentAsString, "id")

        val detalhe = mockMvc.perform(get("/academico/cursos/$id").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.disciplinas").value("/academico/disciplinas?idCurso=$id"))
            .andReturn()
        val href = ObjectMapper().readTree(detalhe.response.contentAsString)
            .path("_links").path("disciplinas").asText()

        mockMvc.perform(get(href).header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
    }

    @Test
    fun preflightOptionsNaoRetorna401() {
        mockMvc.perform(
            options("/academico/cursos")
                .header("Origin", "http://localhost:5174")
                .header("Access-Control-Request-Method", "GET"),
        )
            .andExpect(status().isOk)
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
        return ItJson.text(result.response.contentAsString, "accessToken")
    }

    companion object {
        private const val SENHA = "TroqueEstaSenha1!"
        private const val EMAIL_ALUNO = "it.fgac.aluno@ufpr.br"
        private const val EMAIL_SEC = "it.fgac.sec@ufpr.br"
        private const val EMAIL_SEC_B = "it.fgac.secb@ufpr.br"
        private const val EMAIL_COORD = "it.fgac.coord@ufpr.br"
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

        private fun codigoUnico(prefixo: String): Pair<String, String> {
            val sufixo = Uuids.v7().toString().replace("-", "").take(6).uppercase()
            return prefixo + sufixo.take(2) to "$prefixo-$sufixo"
        }

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
    }
}
