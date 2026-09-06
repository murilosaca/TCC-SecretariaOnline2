package br.ufpr.sept.so2.modules.solicitacoes.api;

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
class SolicitacaoControllerIT {

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
        if (usuarioRepository.findByEmail("it.solicitacao@ufpr.br").isEmpty()) {
            usuarioRepository.save(new Usuario(
                    Uuids.v7(),
                    Email.of("it.solicitacao@ufpr.br"),
                    null,
                    Grr.of("GRR20248888"),
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
        if (usuarioRepository.findByEmail("it.outro@ufpr.br").isEmpty()) {
            usuarioRepository.save(new Usuario(
                    Uuids.v7(),
                    Email.of("it.outro@ufpr.br"),
                    null,
                    Grr.of("GRR20247777"),
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
    }

    @Test
    void criarListarEObterRequerAutenticacao() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isUnauthorized());

        String token = login("it.solicitacao@ufpr.br");

        mockMvc.perform(post("/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoCodigo":"DECLARACAO_SIMPLES","payload":{"finalidade":"abc"}}
                                """))
                .andExpect(status().isUnprocessableEntity());

        MvcResult created = mockMvc.perform(post("/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoCodigo": "DECLARACAO_SIMPLES",
                                  "payload": {
                                    "finalidade": "Comprovação de vínculo",
                                    "observacao": "Para estágio"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.protocolo", startsWith("PROT-")))
                .andExpect(jsonPath("$.estado").value("EM_ANALISE"))
                .andExpect(jsonPath("$.tipoCodigo").value("DECLARACAO_SIMPLES"))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.deliberar").doesNotExist())
                .andExpect(jsonPath("$.eventos[0].tipo").value("CRIADA"))
                .andReturn();

        String id = extract(created.getResponse().getContentAsString(), "\"id\":\"", "\"");

        mockMvc.perform(get("/requests?solicitante=me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$._links.novaSolicitacao").value("/request-types"));

        mockMvc.perform(get("/requests/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventos[0].tipo").value("CRIADA"));

        String outro = login("it.outro@ufpr.br");
        mockMvc.perform(get("/requests/" + id)
                        .header("Authorization", "Bearer " + outro))
                .andExpect(status().isNotFound());
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
