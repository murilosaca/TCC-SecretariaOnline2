package br.ufpr.sept.so2.modules.presenca.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
class EventoControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var eventoRepository: EventoRepository

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        criarUsuarioSeAusente(
            "it.presenca@ufpr.br",
            "GRR20249901",
            true,
            agora,
            agora,
            listOf("dashboard.view_own", "attendance.view_open", "attendance.check_in"),
        )
        criarUsuarioSeAusente(
            "novo.dev@ufpr.br",
            "GRR20240002",
            false,
            null,
            agora,
            listOf("dashboard.view_own", "attendance.view_open", "attendance.check_in"),
        )
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/events?audience=me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun primeiroAcessoRecebe403() {
        val token = login("novo.dev@ufpr.br")
        mockMvc.perform(
            get("/events?audience=me")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun confirmaSecretSingleERejeitaReplayComMesmoDetalhe403() {
        val aberto = salvarEvento(
            AttendanceMode.SECRET_SINGLE,
            OffsetDateTime.now().minusMinutes(1),
            OffsetDateTime.now().plusMinutes(30),
        )
        val token = login("it.presenca@ufpr.br")

        mockMvc.perform(
            get("/events?audience=me")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${aberto.id}')].titulo").exists())
            .andExpect(jsonPath("$.content[?(@.id=='${aberto.id}')]._links['confirmar-entrada']").exists())

        mockMvc.perform(
            get("/events/${aberto.id}/attendance/session")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.attendanceMode").value("SECRET_SINGLE"))
            .andExpect(jsonPath("$.situacaoPresenca").value("PENDENTE"))
            .andExpect(jsonPath("$.janelaAtiva").value(true))
            .andExpect(jsonPath("$._links['confirmar-entrada']").value("/events/${aberto.id}/attendance/confirm"))

        mockMvc.perform(
            post("/events/${aberto.id}/attendance/confirm")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(confirmBody(PIN, DEVICE, "ENTRADA")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.situacaoPresenca").value("COMPLETA"))
            .andExpect(jsonPath("$._links['confirmar-entrada']").doesNotExist())

        mockMvc.perform(
            post("/events/${aberto.id}/attendance/confirm")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(confirmBody(PIN, DEVICE, "ENTRADA")),
        )
            .andExpect(status().isConflict)

        val outroAberto = salvarEvento(
            AttendanceMode.SECRET_SINGLE,
            OffsetDateTime.now().minusMinutes(1),
            OffsetDateTime.now().plusMinutes(30),
        )
        val pinErrado = mockMvc.perform(
            post("/events/${outroAberto.id}/attendance/confirm")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(confirmBody("000000", "22222222-2222-4222-8222-222222222222", "ENTRADA")),
        )
            .andExpect(status().isForbidden)
            .andReturn()
            .response
            .contentAsString

        val fechado = salvarEvento(
            AttendanceMode.SECRET_SINGLE,
            OffsetDateTime.now().minusHours(2),
            OffsetDateTime.now().minusHours(1),
        )
        val janelaFechada = mockMvc.perform(
            post("/events/${fechado.id}/attendance/confirm")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(confirmBody(PIN, "33333333-3333-4333-8333-333333333333", "ENTRADA")),
        )
            .andExpect(status().isForbidden)
            .andReturn()
            .response
            .contentAsString

        assertTrue(pinErrado.contains(Evento.CONFIRMACAO_NEGADA))
        assertTrue(janelaFechada.contains(Evento.CONFIRMACAO_NEGADA))
        assertEquals(extract(pinErrado, "\"detail\":\"", "\""), extract(janelaFechada, "\"detail\":\"", "\""))
    }

    @Test
    fun outroModoRecebe409() {
        val qr = salvarEvento(
            AttendanceMode.QR_SINGLE,
            OffsetDateTime.now().minusMinutes(1),
            OffsetDateTime.now().plusMinutes(30),
        )
        val token = login("it.presenca@ufpr.br")
        mockMvc.perform(
            post("/events/${qr.id}/attendance/confirm")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(confirmBody(PIN, DEVICE, "ENTRADA")),
        )
            .andExpect(status().isConflict)
    }

    private fun salvarEvento(modo: AttendanceMode, janelaInicio: OffsetDateTime, janelaFim: OffsetDateTime): Evento {
        val agora = OffsetDateTime.now()
        return eventoRepository.save(
            Evento(
                Uuids.v7(),
                Uuids.v7(),
                "IT presença ${Uuids.v7()}",
                janelaInicio.minusMinutes(10),
                janelaFim.plusHours(1),
                4,
                modo,
                EventoEstado.EM_ANDAMENTO,
                passwordHasher.hash(PIN),
                janelaInicio,
                janelaFim,
                null,
                null,
                agora,
                agora,
            ),
        )
    }

    private fun criarUsuarioSeAusente(
        email: String,
        grr: String,
        senhaAlterada: Boolean,
        lgpd: OffsetDateTime?,
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
                lgpd,
                if (lgpd == null) null else "127.0.0.1",
                if (lgpd == null) null else "it",
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
        private const val PIN = "123456"
        private const val DEVICE = "11111111-1111-4111-8111-111111111111"

        private fun confirmBody(pin: String, deviceUuid: String, fase: String): String =
            "{\"pin\":\"$pin\",\"deviceUuid\":\"$deviceUuid\",\"fase\":\"$fase\"}"

        private fun extract(json: String, startToken: String, endToken: String): String {
            val start = json.indexOf(startToken)
            val from = start + startToken.length
            val end = json.indexOf(endToken, from)
            return json.substring(from, end)
        }
    }
}
