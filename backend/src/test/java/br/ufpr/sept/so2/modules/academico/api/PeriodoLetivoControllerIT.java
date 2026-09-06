package br.ufpr.sept.so2.modules.academico.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PeriodoLetivoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void criaListaERecusaSobreposicao() throws Exception {
        mockMvc.perform(post("/academico/periodos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ano": 2026,
                                  "semestre": 1,
                                  "inicio": "2026-02-01",
                                  "fim": "2026-07-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.semestre").value(1))
                .andExpect(jsonPath("$._links.self").exists());

        mockMvc.perform(post("/academico/periodos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ano": 2026,
                                  "semestre": 2,
                                  "inicio": "2026-07-01",
                                  "fim": "2026-12-20"
                                }
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/academico/periodos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }
}
