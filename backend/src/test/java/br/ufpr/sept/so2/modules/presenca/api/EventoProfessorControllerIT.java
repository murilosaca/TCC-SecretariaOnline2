package br.ufpr.sept.so2.modules.presenca.api;

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.modules.presenca.domain.Evento;
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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventoProfessorControllerIT {

    private static final String SENHA = "TroqueEstaSenha1!";
    private static final String DEVICE = "44444444-4444-4444-8444-444444444444";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @BeforeEach
    void seed() {
        OffsetDateTime agora = OffsetDateTime.now();
        criarUsuarioSeAusente(
                "it.professor@ufpr.br",
                "GRR20249910",
                true,
                agora,
                List.of("dashboard.view_own", "event.manage", "event.host")
        );
        criarUsuarioSeAusente(
                "it.presenca@ufpr.br",
                "GRR20249901",
                true,
                agora,
                List.of("dashboard.view_own", "attendance.view_open", "attendance.check_in")
        );
        criarUsuarioSeAusente(
                "novo.dev@ufpr.br",
                "GRR20240002",
                false,
                agora,
                List.of("dashboard.view_own", "attendance.view_open", "attendance.check_in")
        );
    }

    @Test
    void professorAbreJanelaAlunoConfirmaComPinDaHostSession() throws Exception {
        String professor = login("it.professor@ufpr.br");
        OffsetDateTime inicio = OffsetDateTime.now().plusMinutes(10);
        OffsetDateTime fim = inicio.plusHours(2);

        MvcResult criado = mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + professor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarBody("Oficina IT professor", inicio, fim)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attendanceMode").value("SECRET_SINGLE"))
                .andExpect(jsonPath("$.estado").value("AGENDADO"))
                .andExpect(jsonPath("$.pin").doesNotExist())
                .andExpect(jsonPath("$._links['abrir-janela-entrada']").exists())
                .andReturn();
        String eventoId = extract(criado.getResponse().getContentAsString(), "\"id\":\"", "\"");

        mockMvc.perform(get("/events?mine=true")
                        .header("Authorization", "Bearer " + professor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id=='" + eventoId + "')].titulo").exists())
                .andExpect(jsonPath("$._links.novoEvento").value("/events"));

        MvcResult host = mockMvc.perform(post("/events/" + eventoId + "/attendance/windows/entry")
                        .header("Authorization", "Bearer " + professor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.janelaAtiva").value(true))
                .andExpect(jsonPath("$.pin", not(nullValue())))
                .andExpect(jsonPath("$._links['encerrar-evento']").exists())
                .andReturn();
        String pin = extract(host.getResponse().getContentAsString(), "\"pin\":\"", "\"");

        mockMvc.perform(get("/events/" + eventoId + "/attendance/host-session")
                        .header("Authorization", "Bearer " + professor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pin").value(pin));

        String aluno = login("it.presenca@ufpr.br");
        mockMvc.perform(get("/events/" + eventoId + "/attendance/host-session")
                        .header("Authorization", "Bearer " + aluno))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/events/" + eventoId + "/attendance/confirm")
                        .header("Authorization", "Bearer " + aluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\":\"" + pin + "\",\"deviceUuid\":\"" + DEVICE + "\",\"fase\":\"ENTRADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoPresenca").value("COMPLETA"))
                .andExpect(jsonPath("$._links['confirmar-entrada']").doesNotExist());
    }

    @Test
    void encerrarFechaJanelaParaOAluno() throws Exception {
        String professor = login("it.professor@ufpr.br");
        OffsetDateTime inicio = OffsetDateTime.now().plusMinutes(5);
        String eventoId = extract(mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + professor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarBody("Oficina IT encerrar", inicio, inicio.plusHours(2))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(), "\"id\":\"", "\"");

        MvcResult host = mockMvc.perform(post("/events/" + eventoId + "/attendance/windows/entry")
                        .header("Authorization", "Bearer " + professor))
                .andExpect(status().isOk())
                .andReturn();
        String pin = extract(host.getResponse().getContentAsString(), "\"pin\":\"", "\"");

        mockMvc.perform(post("/events/" + eventoId + "/encerrar")
                        .header("Authorization", "Bearer " + professor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONCLUIDO"))
                .andExpect(jsonPath("$.janelaAtiva").value(false))
                .andExpect(jsonPath("$.pin").doesNotExist())
                .andExpect(jsonPath("$._links['abrir-janela-entrada']").doesNotExist())
                .andExpect(jsonPath("$._links['encerrar-evento']").doesNotExist());

        String aluno = login("it.presenca@ufpr.br");
        mockMvc.perform(get("/events/" + eventoId + "/attendance/session")
                        .header("Authorization", "Bearer " + aluno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.janelaAtiva").value(false))
                .andExpect(jsonPath("$._links['confirmar-entrada']").doesNotExist());
        mockMvc.perform(post("/events/" + eventoId + "/attendance/confirm")
                        .header("Authorization", "Bearer " + aluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\":\"" + pin + "\",\"deviceUuid\":\"" + DEVICE + "\",\"fase\":\"ENTRADA\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value(Evento.CONFIRMACAO_NEGADA));
    }

    @Test
    void alunoNaoListaMineENovoDevContinua403() throws Exception {
        String aluno = login("it.presenca@ufpr.br");
        mockMvc.perform(get("/events?mine=true")
                        .header("Authorization", "Bearer " + aluno))
                .andExpect(status().isForbidden());

        String novo = login("novo.dev@ufpr.br");
        mockMvc.perform(get("/events?mine=true")
                        .header("Authorization", "Bearer " + novo))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/events?audience=me")
                        .header("Authorization", "Bearer " + novo))
                .andExpect(status().isForbidden());
    }

    @Test
    void janelaDeSaidaRecebe409() throws Exception {
        String professor = login("it.professor@ufpr.br");
        OffsetDateTime inicio = OffsetDateTime.now().plusMinutes(5);
        String eventoId = extract(mockMvc.perform(post("/events")
                        .header("Authorization", "Bearer " + professor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarBody("Oficina IT saida", inicio, inicio.plusHours(1))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(), "\"id\":\"", "\"");
        mockMvc.perform(post("/events/" + eventoId + "/attendance/windows/exit")
                        .header("Authorization", "Bearer " + professor))
                .andExpect(status().isConflict());
    }

    private void criarUsuarioSeAusente(
            String email,
            String grr,
            boolean senhaAlterada,
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
                senhaAlterada ? agora : null,
                senhaAlterada ? "127.0.0.1" : null,
                senhaAlterada ? "it" : null,
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

    private static String criarBody(String titulo, OffsetDateTime inicio, OffsetDateTime fim) {
        return "{\"titulo\":\"" + titulo + "\",\"inicioEm\":\"" + inicio + "\",\"fimEm\":\"" + fim + "\",\"cargaHoraria\":4}";
    }

    private static String extract(String json, String startToken, String endToken) {
        int start = json.indexOf(startToken);
        int from = start + startToken.length();
        int end = json.indexOf(endToken, from);
        return json.substring(from, end);
    }
}
