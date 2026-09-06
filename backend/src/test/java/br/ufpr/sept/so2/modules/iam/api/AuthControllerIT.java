package br.ufpr.sept.so2.modules.iam.api;

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
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @BeforeEach
    void seed() {
        if (usuarioRepository.findByEmail("it.login@ufpr.br").isPresent()) {
            return;
        }
        OffsetDateTime agora = OffsetDateTime.now();
        usuarioRepository.save(new Usuario(
                Uuids.v7(),
                Email.of("it.login@ufpr.br"),
                Email.of("pessoal.it@gmail.com"),
                Grr.of("GRR20249999"),
                passwordHasher.hash("TroqueEstaSenha1!"),
                true,
                agora,
                "127.0.0.1",
                "it",
                true,
                0,
                null,
                List.of("dashboard.view_own"),
                agora,
                agora
        ));
    }

    @Test
    void loginSucessoComEmailEGrr() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identificador":"it.login@ufpr.br","senha":"TroqueEstaSenha1!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.mustChangePassword").value(false))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(cookie().exists("so2_refresh"))
                .andExpect(cookie().httpOnly("so2_refresh", true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identificador":"GRR20249999","senha":"TroqueEstaSenha1!"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void loginFalhaAntiEnumeracao() throws Exception {
        String inexistente = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identificador":"sumido@ufpr.br","senha":"qualquer"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value(
                        "Credenciais inválidas. Verifique seus dados e tente novamente."))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String senhaErrada = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identificador":"it.login@ufpr.br","senha":"errada"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value(
                        "Credenciais inválidas. Verifique seus dados e tente novamente."))
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.junit.jupiter.api.Assertions.assertEquals(
                extractDetail(inexistente),
                extractDetail(senhaErrada)
        );
    }

    @Test
    void recuperarSenhaSempreAccepted() throws Exception {
        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"it.login@ufpr.br"}
                                """))
                .andExpect(status().isAccepted());
        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"naoexiste@ufpr.br"}
                                """))
                .andExpect(status().isAccepted());
    }

    private static String extractDetail(String json) {
        int start = json.indexOf("\"detail\":\"");
        return json.substring(start);
    }
}
