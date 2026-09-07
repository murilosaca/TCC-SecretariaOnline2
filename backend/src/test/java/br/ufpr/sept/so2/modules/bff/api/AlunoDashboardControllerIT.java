package br.ufpr.sept.so2.modules.bff.api;

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository;
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed;
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

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlunoDashboardControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private TipoSolicitacaoRepository tipoSolicitacaoRepository;

    @BeforeEach
    void seed() {
        OffsetDateTime agora = OffsetDateTime.now();
        if (tipoSolicitacaoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty()) {
            tipoSolicitacaoRepository.save(DeclaracaoSimplesSeed.tipo(agora));
        }
        criarUsuarioSeAusente(
                "aluno.dev@ufpr.br",
                "GRR20240001",
                true,
                agora,
                agora,
                List.of("dashboard.view_own", "request.view_own", "request.open")
        );
        criarUsuarioSeAusente(
                "novo.dev@ufpr.br",
                "GRR20240002",
                false,
                null,
                agora,
                List.of("dashboard.view_own", "request.view_own", "request.open")
        );
        criarUsuarioSeAusente(
                "professor.dashboard@ufpr.br",
                "GRR20240099",
                true,
                agora,
                agora,
                List.of("dashboard.view_own", "event.manage", "event.host")
        );
    }

    @Test
    void anonimoRecebe401() throws Exception {
        mockMvc.perform(get("/bff/dashboard/aluno"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void primeiroAcessoRecebe403() throws Exception {
        String token = login("novo.dev@ufpr.br");
        mockMvc.perform(get("/bff/dashboard/aluno")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoAutenticadoVeProtocoloQuandoExiste() throws Exception {
        String token = login("aluno.dev@ufpr.br");

        mockMvc.perform(post("/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoCodigo": "DECLARACAO_SIMPLES",
                                  "payload": {
                                    "finalidade": "Comprovação de vínculo",
                                    "observacao": "Dashboard"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.protocolo", startsWith("PROT-")));

        mockMvc.perform(get("/bff/dashboard/aluno")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saudacao.nome").value("Aluno Dev"))
                .andExpect(jsonPath("$.kpis.horasFormativas").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.kpis.eventosHoje").isNumber())
                .andExpect(jsonPath("$.kpis.certificados").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.kpis.solicitacoesAbertas").isNumber())
                .andExpect(jsonPath("$.proximosEventos").isArray())
                .andExpect(jsonPath("$.ultimasSolicitacoes[0].protocolo", startsWith("PROT-")))
                .andExpect(jsonPath("$._links.self").value("/bff/dashboard/aluno"))
                .andExpect(jsonPath("$._links.novaSolicitacao").value("/solicitacoes/nova"));
    }

    @Test
    void sessaoSemCapabilityDeAlunoRecebe403() throws Exception {
        String token = login("professor.dashboard@ufpr.br");
        mockMvc.perform(get("/bff/dashboard/aluno")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
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
                passwordHasher.hash("TroqueEstaSenha1!"),
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
                        .content("{\"identificador\":\"" + email + "\",\"senha\":\"TroqueEstaSenha1!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return extract(result.getResponse().getContentAsString(), "\"accessToken\":\"", "\"");
    }

    private static String extract(String json, String startToken, String endToken) {
        int start = json.indexOf(startToken);
        int from = start + startToken.length();
        int end = json.indexOf(endToken, from);
        return json.substring(from, end);
    }
}
