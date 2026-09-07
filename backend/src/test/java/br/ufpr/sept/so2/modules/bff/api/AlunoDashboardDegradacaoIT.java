package br.ufpr.sept.so2.modules.bff.api;

import br.ufpr.sept.so2.modules.bff.application.ports.EventosDashboardQueryPort;
import br.ufpr.sept.so2.modules.bff.application.ports.SolicitacoesDashboardQueryPort;
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlunoDashboardDegradacaoIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @MockitoBean
    private SolicitacoesDashboardQueryPort solicitacoesDashboardQueryPort;

    @MockitoBean
    private EventosDashboardQueryPort eventosDashboardQueryPort;

    @BeforeEach
    void seed() {
        when(solicitacoesDashboardQueryPort.consultar(any(UUID.class)))
                .thenThrow(new IllegalStateException("módulo de solicitações indisponível"));
        when(eventosDashboardQueryPort.consultar(any()))
                .thenThrow(new IllegalStateException("módulo de presença indisponível"));
        if (usuarioRepository.findByEmail("it.dashboard.degradacao@ufpr.br").isPresent()) {
            return;
        }
        OffsetDateTime agora = OffsetDateTime.now();
        usuarioRepository.save(new Usuario(
                Uuids.v7(),
                Email.of("it.dashboard.degradacao@ufpr.br"),
                null,
                Grr.of("GRR20246666"),
                passwordHasher.hash("TroqueEstaSenha1!"),
                true,
                agora,
                "127.0.0.1",
                "it",
                true,
                0,
                null,
                List.of("dashboard.view_own", "request.view_own", "request.open"),
                agora,
                agora
        ));
    }

    @Test
    void blocoNuloNaoDerruba200() throws Exception {
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identificador":"it.dashboard.degradacao@ufpr.br","senha":"TroqueEstaSenha1!"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String token = extract(login.getResponse().getContentAsString(), "\"accessToken\":\"", "\"");

        mockMvc.perform(get("/bff/dashboard/aluno")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saudacao.nome").exists())
                .andExpect(jsonPath("$.kpis.horasFormativas").value(nullValue()))
                .andExpect(jsonPath("$.kpis.solicitacoesAbertas").value(nullValue()))
                .andExpect(jsonPath("$.kpis.eventosHoje").value(nullValue()))
                .andExpect(jsonPath("$.pendencias").value(nullValue()))
                .andExpect(jsonPath("$.ultimasSolicitacoes").value(nullValue()))
                .andExpect(jsonPath("$.proximosEventos").value(nullValue()))
                .andExpect(jsonPath("$._links.self").value("/bff/dashboard/aluno"));
    }

    private static String extract(String json, String startToken, String endToken) {
        int start = json.indexOf(startToken);
        int from = start + startToken.length();
        int end = json.indexOf(endToken, from);
        return json.substring(from, end);
    }
}
