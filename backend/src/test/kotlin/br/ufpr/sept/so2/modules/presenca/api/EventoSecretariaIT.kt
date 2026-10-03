package br.ufpr.sept.so2.modules.presenca.api

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.PresencaRepository
import br.ufpr.sept.so2.modules.presenca.domain.FasePresenca
import br.ufpr.sept.so2.modules.presenca.domain.Presenca
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.infrastructure.Uuids
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventoSecretariaIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var cursoRepository: CursoRepository

    @Autowired
    private lateinit var cursoSecretarioRepository: CursoSecretarioRepository

    @Autowired
    private lateinit var presencaRepository: PresencaRepository

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoId: UUID
    private lateinit var cursoForaId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.parse("2026-10-03T12:00:00Z")
        cursoId = garantirCurso(CODIGO, "TADS Ev Sec", "EVSC", agora)
        cursoForaId = garantirCurso(CODIGO_FORA, "Outro Ev Sec", "OEVS", agora)
        val secretaria = usuariosIt.criarUsuario(
            EMAIL_SEC,
            GRR_SEC,
            listOf("event.manage", "event.host", "event.view_curso"),
        )
        cursoSecretarioRepository.adicionarSeAusente(cursoId, secretaria.id)
        usuariosIt.criarUsuario(EMAIL_SEM_CAP, GRR_SEM_CAP, listOf("course.manage"))
        usuariosIt.criarUsuario(EMAIL_PROF, GRR_PROF, listOf("event.manage", "event.host"))
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/events").param("escopo", "cursos"))
            .andExpect(status().isUnauthorized)
        mockMvc.perform(delete("/events/${UUID.randomUUID()}"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityRecebe403() {
        val token = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(get("/events").param("escopo", "cursos").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
        mockMvc.perform(
            post("/events")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(criarBody("Sem cap", cursoId)),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun cursoForaDoEscopoRecebe403EProfessorContinuaSemCurso() {
        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(
            get("/events")
                .param("escopo", "cursos")
                .param("cursoId", cursoForaId.toString())
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Curso fora do escopo da sua secretaria."))

        mockMvc.perform(
            post("/events")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(criarBody("Fora", cursoForaId)),
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Curso fora do escopo da sua secretaria."))

        val professor = usuariosIt.login(EMAIL_PROF)
        mockMvc.perform(
            post("/events")
                .header("Authorization", "Bearer $professor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(criarBody("Oficina professor", null)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.idCurso").doesNotExist())
            .andExpect(jsonPath("$._links.editar").doesNotExist())
    }

    @Test
    fun deleteComPresencaRecebe422EConcluidoRecebe409() {
        val token = usuariosIt.login(EMAIL_SEC)
        val comPresenca = criarEvento(token, "Com presença")
        presencaRepository.save(
            Presenca.registrar(
                Uuids.v7(),
                UUID.fromString(comPresenca),
                usuarioRepository.findByEmail(EMAIL_SEC).get().id,
                FasePresenca.ENTRADA,
                "device-it-sec",
                OffsetDateTime.now(),
            ),
        )
        mockMvc.perform(delete("/events/$comPresenca").header("Authorization", "Bearer $token"))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value("https://secretariaonline.ufpr.br/errors/validation-error"))
            .andExpect(jsonPath("$.detail").value("Evento possui registros de presença"))

        val aEncerrar = criarEvento(token, "A encerrar")
        mockMvc.perform(
            post("/events/$aEncerrar/attendance/windows/entry").header("Authorization", "Bearer $token"),
        ).andExpect(status().isOk)
        mockMvc.perform(
            post("/events/$aEncerrar/encerrar").header("Authorization", "Bearer $token"),
        ).andExpect(status().isOk)
        mockMvc.perform(get("/events").param("escopo", "cursos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='$aEncerrar')]._links.editar").doesNotExist())
            .andExpect(jsonPath("$.content[?(@.id=='$aEncerrar')]._links.excluir").doesNotExist())
        mockMvc.perform(delete("/events/$aEncerrar").header("Authorization", "Bearer $token"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.type").value("https://secretariaonline.ufpr.br/errors/conflict"))
    }

    private fun criarEvento(token: String, titulo: String): String {
        val criado = mockMvc.perform(
            post("/events")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(criarBody(titulo, cursoId)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.estado").value("AGENDADO"))
            .andExpect(jsonPath("$._links.editar").exists())
            .andExpect(jsonPath("$._links.excluir").exists())
            .andReturn()
        return ItJson.text(criado.response.contentAsString, "id")
    }

    private fun criarBody(titulo: String, curso: UUID?): String {
        val inicio = OffsetDateTime.now().plusMinutes(10)
        val fim = inicio.plusHours(2)
        val cursoJson = if (curso == null) "" else ",\"cursoId\":\"$curso\""
        return """
            {
              "titulo": "$titulo",
              "inicioEm": "$inicio",
              "fimEm": "$fim",
              "cargaHoraria": 2,
              "attendanceMode": "SECRET_SINGLE"
              $cursoJson
            }
        """.trimIndent()
    }

    private fun garantirCurso(codigo: String, nome: String, sigla: String, agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(Curso(Uuids.v7(), nome, sigla, codigo, null, 120, true, agora, agora)).id
    }

    companion object {
        private const val EMAIL_SEC = "it.evsec.sec@ufpr.br"
        private const val EMAIL_SEM_CAP = "it.evsec.semcap@ufpr.br"
        private const val EMAIL_PROF = "it.evsec.prof@ufpr.br"
        private const val GRR_SEC = "GRR20249701"
        private const val GRR_SEM_CAP = "GRR20249702"
        private const val GRR_PROF = "GRR20249703"
        private const val CODIGO = "EVSEC-IT"
        private const val CODIGO_FORA = "OEVS-IT"
    }
}
