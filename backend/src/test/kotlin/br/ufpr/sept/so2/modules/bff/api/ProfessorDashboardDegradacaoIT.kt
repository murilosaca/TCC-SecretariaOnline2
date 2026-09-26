package br.ufpr.sept.so2.modules.bff.api

import br.ufpr.sept.so2.modules.bff.application.ports.DeliberacaoDashboardQueryPort
import br.ufpr.sept.so2.modules.bff.application.ports.EventosProfessorDashboardQueryPort
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfessorDashboardDegradacaoIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @MockitoBean
    private lateinit var deliberacaoDashboardQueryPort: DeliberacaoDashboardQueryPort

    @MockitoBean
    private lateinit var eventosProfessorDashboardQueryPort: EventosProfessorDashboardQueryPort

    @BeforeEach
    fun seed() {
        `when`(deliberacaoDashboardQueryPort.consultar())
            .thenThrow(IllegalStateException("módulo de solicitações indisponível"))
        `when`(eventosProfessorDashboardQueryPort.consultar(anyUuid(), anyMomento()))
            .thenThrow(IllegalStateException("módulo de presença indisponível"))
        if (usuarioRepository.findByEmail("it.professor.degradacao@ufpr.br").isPresent) {
            return
        }
        val agora = OffsetDateTime.now()
        usuarioRepository.save(
            Usuario(
                Uuids.v7(),
                "Professor Degradacao",
                Email.of("it.professor.degradacao@ufpr.br"),
                null,
                Grr.of("GRR20246667"),
                passwordHasher.hash("TroqueEstaSenha1!"),
                true,
                agora,
                "127.0.0.1",
                "it",
                true,
                0,
                null,
                listOf(
                    "dashboard.view_self_professor",
                    "event.manage",
                    "request.deliberate",
                ),
                agora,
                agora,
            ),
        )
    }

    @Test
    fun blocoNuloNaoDerruba200() {
        val login = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"identificador":"it.professor.degradacao@ufpr.br","senha":"TroqueEstaSenha1!"}
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andReturn()
        val token = extract(login.response.contentAsString, "\"accessToken\":\"", "\"")

        mockMvc.perform(
            get("/bff/dashboard/professor")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.saudacao.nome").exists())
            .andExpect(jsonPath("$.kpis.pendentesDeliberar").value(nullValue()))
            .andExpect(jsonPath("$.kpis.slaUrgentes").value(nullValue()))
            .andExpect(jsonPath("$.kpis.eventosHoje").value(nullValue()))
            .andExpect(jsonPath("$.filaSolicitacoes").value(nullValue()))
            .andExpect(jsonPath("$.meusEventos").value(nullValue()))
            .andExpect(jsonPath("$._links.self").value("/bff/dashboard/professor"))
    }

    companion object {
        private fun extract(json: String, startToken: String, endToken: String): String {
            val start = json.indexOf(startToken)
            val from = start + startToken.length
            val end = json.indexOf(endToken, from)
            return json.substring(from, end)
        }

        @Suppress("UNCHECKED_CAST")
        private fun anyUuid(): UUID {
            any(UUID::class.java)
            return UUID(0, 0)
        }

        @Suppress("UNCHECKED_CAST")
        private fun anyMomento(): OffsetDateTime {
            any(OffsetDateTime::class.java)
            return OffsetDateTime.MIN
        }
    }
}
