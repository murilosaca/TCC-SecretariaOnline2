package br.ufpr.sept.so2.modules.presenca.api;

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository;
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.domain.valueobject.Grr;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventoControllerIT {

    private static final String SENHA = "TroqueEstaSenha1!";
    private static final String PIN = "123456";
    private static final String DEVICE = "11111111-1111-4111-8111-111111111111";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private EventoRepository eventoRepository;

    @BeforeEach
    void seed() {
        OffsetDateTime agora = OffsetDateTime.now();
        criarUsuarioSeAusente(
                "it.presenca@ufpr.br",
                "GRR20249901",
                true,
                agora,
                agora,
                List.of("dashboard.view_own", "attendance.view_open", "attendance.check_in")
        );
        criarUsuarioSeAusente(
                "novo.dev@ufpr.br",
                "GRR20240002",
                false,
                null,
                agora,
                List.of("dashboard.view_own", "attendance.view_open", "attendance.check_in")
        );
    }

    @Test
    void anonimoRecebe401() throws Exception {
        mockMvc.perform(get("/events?audience=me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void primeiroAcessoRecebe403() throws Exception {
        String token = login("novo.dev@ufpr.br");
        mockMvc.perform(get("/events?audience=me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void confirmaSecretSingleERejeitaReplayComMesmoDetalhe403() throws Exception {
        Evento aberto = salvarEvento(AttendanceMode.SECRET_SINGLE, OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now().plusMinutes(30));
        String token = login("it.presenca@ufpr.br");

        mockMvc.perform(get("/events?audience=me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id=='" + aberto.getId() + "')].titulo").exists())
                .andExpect(jsonPath("$.content[?(@.id=='" + aberto.getId() + "')]._links['confirmar-entrada']").exists());

        mockMvc.perform(get("/events/" + aberto.getId() + "/attendance/session")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceMode").value("SECRET_SINGLE"))
                .andExpect(jsonPath("$.situacaoPresenca").value("PENDENTE"))
                .andExpect(jsonPath("$.janelaAtiva").value(true))
                .andExpect(jsonPath("$._links['confirmar-entrada']").value("/events/" + aberto.getId() + "/attendance/confirm"));

        mockMvc.perform(post("/events/" + aberto.getId() + "/attendance/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody(PIN, DEVICE, "ENTRADA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoPresenca").value("COMPLETA"))
                .andExpect(jsonPath("$._links['confirmar-entrada']").doesNotExist());

        mockMvc.perform(post("/events/" + aberto.getId() + "/attendance/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody(PIN, DEVICE, "ENTRADA")))
                .andExpect(status().isConflict());

        Evento outroAberto = salvarEvento(
                AttendanceMode.SECRET_SINGLE,
                OffsetDateTime.now().minusMinutes(1),
                OffsetDateTime.now().plusMinutes(30)
        );
        String pinErrado = mockMvc.perform(post("/events/" + outroAberto.getId() + "/attendance/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody("000000", "22222222-2222-4222-8222-222222222222", "ENTRADA")))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Evento fechado = salvarEvento(
                AttendanceMode.SECRET_SINGLE,
                OffsetDateTime.now().minusHours(2),
                OffsetDateTime.now().minusHours(1)
        );
        String janelaFechada = mockMvc.perform(post("/events/" + fechado.getId() + "/attendance/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody(PIN, "33333333-3333-4333-8333-333333333333", "ENTRADA")))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(pinErrado.contains(Evento.CONFIRMACAO_NEGADA));
        org.junit.jupiter.api.Assertions.assertTrue(janelaFechada.contains(Evento.CONFIRMACAO_NEGADA));
        org.junit.jupiter.api.Assertions.assertEquals(
                extract(pinErrado, "\"detail\":\"", "\""),
                extract(janelaFechada, "\"detail\":\"", "\"")
        );
    }

    @Test
    void outroModoRecebe409() throws Exception {
        Evento qr = salvarEvento(AttendanceMode.QR_SINGLE, OffsetDateTime.now().minusMinutes(1), OffsetDateTime.now().plusMinutes(30));
        String token = login("it.presenca@ufpr.br");
        mockMvc.perform(post("/events/" + qr.getId() + "/attendance/confirm")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody(PIN, DEVICE, "ENTRADA")))
                .andExpect(status().isConflict());
    }

    private Evento salvarEvento(AttendanceMode modo, OffsetDateTime janelaInicio, OffsetDateTime janelaFim) {
        OffsetDateTime agora = OffsetDateTime.now();
        return eventoRepository.save(new Evento(
                Uuids.v7(),
                Uuids.v7(),
                "IT presença " + Uuids.v7(),
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
                agora
        ));
    }

    private void criarUsuarioSeAusente(
            String email,
            String grr,
            boolean senhaAlterada,
            OffsetDateTime lgpd,
            OffsetDateTime agora,
            List<String> authorities
    ) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return;
        }
        usuarioRepository.save(new Usuario(
                Uuids.v7(),
                Email.of(email),
                null,
                Grr.of(grr),
                passwordHasher.hash(SENHA),
                senhaAlterada,
                lgpd,
                lgpd == null ? null : "127.0.0.1",
                lgpd == null ? null : "it",
                true,
                0,
                null,
                authorities,
                agora,
                agora
        ));
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identificador\":\"" + email + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return extract(result.getResponse().getContentAsString(), "\"accessToken\":\"", "\"");
    }

    private static String confirmBody(String pin, String deviceUuid, String fase) {
        return "{\"pin\":\"" + pin + "\",\"deviceUuid\":\"" + deviceUuid + "\",\"fase\":\"" + fase + "\"}";
    }

    private static String extract(String json, String startToken, String endToken) {
        int start = json.indexOf(startToken);
        int from = start + startToken.length();
        int end = json.indexOf(endToken, from);
        return json.substring(from, end);
    }
}
