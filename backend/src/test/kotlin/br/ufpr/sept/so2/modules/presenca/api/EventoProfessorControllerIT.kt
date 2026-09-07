package br.ufpr.sept.so2.modules.presenca.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
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
class EventoProfessorControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        criarUsuarioSeAusente(
            "it.professor@ufpr.br",
            "GRR20249910",
            true,
            agora,
            listOf("dashboard.view_own", "event.manage", "event.host"),
        )
        criarUsuarioSeAusente(
            "it.presenca@ufpr.br",
            "GRR20249901",
            true,
            agora,
            listOf("dashboard.view_own", "attendance.view_open", "attendance.check_in"),
        )
        criarUsuarioSeAusente(
            "novo.dev@ufpr.br",
            "GRR20240002",
            false,
            agora,
            listOf("dashboard.view_own", "attendance.view_open", "attendance.check_in"),
        )
    }

    @Test
    fun professorAbreJanelaAlunoConfirmaComPinDaHostSession() {
        val professor = login("it.professor@ufpr.br")
        val inicio = OffsetDateTime.now().plusMinutes(10)
        val fim = inicio.plusHours(2)

        val criado = mockMvc.perform(
            post("/events")
                .header("Authorization", "Bearer $professor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(criarBody("Oficina IT professor", inicio, fim)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.attendanceMode").value("SECRET_SINGLE"))
            .andExpect(jsonPath("$.estado").value("AGENDADO"))
            .andExpect(jsonPath("$.pin").doesNotExist())
            .andExpect(jsonPath("$._links['abrir-janela-entrada']").exists())
            .andReturn()
        val eventoId = extract(criado.response.contentAsString, "\"id\":\"", "\"")

        mockMvc.perform(
            get("/events?mine=true")
                .header("Authorization", "Bearer $professor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='$eventoId')].titulo").exists())
            .andExpect(jsonPath("$._links.novoEvento").value("/events"))

        val host = mockMvc.perform(
            post("/events/$eventoId/attendance/windows/entry")
                .header("Authorization", "Bearer $professor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("EM_ANDAMENTO"))
            .andExpect(jsonPath("$.janelaAtiva").value(true))
            .andExpect(jsonPath("$.pin", not(nullValue())))
            .andExpect(jsonPath("$._links['encerrar-evento']").exists())
            .andReturn()
        val pin = extract(host.response.contentAsString, "\"pin\":\"", "\"")

        mockMvc.perform(
            get("/events/$eventoId/attendance/host-session")
                .header("Authorization", "Bearer $professor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.pin").value(pin))

        val aluno = login("it.presenca@ufpr.br")
        mockMvc.perform(
            get("/events/$eventoId/attendance/host-session")
                .header("Authorization", "Bearer $aluno"),
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            post("/events/$eventoId/attendance/confirm")
                .header("Authorization", "Bearer $aluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pin\":\"$pin\",\"deviceUuid\":\"$DEVICE\",\"fase\":\"ENTRADA\"}"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.situacaoPresenca").value("COMPLETA"))
            .andExpect(jsonPath("$._links['confirmar-entrada']").doesNotExist())
    }

    @Test
    fun encerrarFechaJanelaParaOAluno() {
        val professor = login("it.professor@ufpr.br")
        val inicio = OffsetDateTime.now().plusMinutes(5)
        val eventoId = extract(
            mockMvc.perform(
                post("/events")
                    .header("Authorization", "Bearer $professor")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(criarBody("Oficina IT encerrar", inicio, inicio.plusHours(2))),
            )
                .andExpect(status().isCreated)
                .andReturn()
                .response
                .contentAsString,
            "\"id\":\"",
            "\"",
        )

        val host = mockMvc.perform(
            post("/events/$eventoId/attendance/windows/entry")
                .header("Authorization", "Bearer $professor"),
        )
            .andExpect(status().isOk)
            .andReturn()
        val pin = extract(host.response.contentAsString, "\"pin\":\"", "\"")

        mockMvc.perform(
            post("/events/$eventoId/encerrar")
                .header("Authorization", "Bearer $professor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("CONCLUIDO"))
            .andExpect(jsonPath("$.janelaAtiva").value(false))
            .andExpect(jsonPath("$.pin").doesNotExist())
            .andExpect(jsonPath("$._links['abrir-janela-entrada']").doesNotExist())
            .andExpect(jsonPath("$._links['encerrar-evento']").doesNotExist())

        val aluno = login("it.presenca@ufpr.br")
        mockMvc.perform(
            get("/events/$eventoId/attendance/session")
                .header("Authorization", "Bearer $aluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.janelaAtiva").value(false))
            .andExpect(jsonPath("$._links['confirmar-entrada']").doesNotExist())
        mockMvc.perform(
            post("/events/$eventoId/attendance/confirm")
                .header("Authorization", "Bearer $aluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pin\":\"$pin\",\"deviceUuid\":\"$DEVICE\",\"fase\":\"ENTRADA\"}"),
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value(Evento.CONFIRMACAO_NEGADA))
    }

    @Test
    fun alunoNaoListaMineENovoDevContinua403() {
        val aluno = login("it.presenca@ufpr.br")
        mockMvc.perform(
            get("/events?mine=true")
                .header("Authorization", "Bearer $aluno"),
        )
            .andExpect(status().isForbidden)

        val novo = login("novo.dev@ufpr.br")
        mockMvc.perform(
            get("/events?mine=true")
                .header("Authorization", "Bearer $novo"),
        )
            .andExpect(status().isForbidden)
        mockMvc.perform(
            get("/events?audience=me")
                .header("Authorization", "Bearer $novo"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun janelaDeSaidaRecebe409() {
        val professor = login("it.professor@ufpr.br")
        val inicio = OffsetDateTime.now().plusMinutes(5)
        val eventoId = extract(
            mockMvc.perform(
                post("/events")
                    .header("Authorization", "Bearer $professor")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(criarBody("Oficina IT saida", inicio, inicio.plusHours(1))),
            )
                .andExpect(status().isCreated)
                .andReturn()
                .response
                .contentAsString,
            "\"id\":\"",
            "\"",
        )
        mockMvc.perform(
            post("/events/$eventoId/attendance/windows/exit")
                .header("Authorization", "Bearer $professor"),
        )
            .andExpect(status().isConflict)
    }

    private fun criarUsuarioSeAusente(
        email: String,
        grr: String,
        senhaAlterada: Boolean,
        agora: OffsetDateTime,
        authorities: List<String>,
    ) {
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
                senhaAlterada,
                if (senhaAlterada) agora else null,
                if (senhaAlterada) "127.0.0.1" else null,
                if (senhaAlterada) "it" else null,
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
        private const val DEVICE = "44444444-4444-4444-8444-444444444444"

        private fun criarBody(titulo: String, inicio: OffsetDateTime, fim: OffsetDateTime): String =
            "{\"titulo\":\"$titulo\",\"inicioEm\":\"$inicio\",\"fimEm\":\"$fim\",\"cargaHoraria\":4}"

        private fun extract(json: String, startToken: String, endToken: String): String {
            val start = json.indexOf(startToken)
            val from = start + startToken.length
            val end = json.indexOf(endToken, from)
            return json.substring(from, end)
        }
    }
}
