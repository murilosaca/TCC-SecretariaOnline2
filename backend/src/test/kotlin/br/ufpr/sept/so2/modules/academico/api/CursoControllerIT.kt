package br.ufpr.sept.so2.modules.academico.api

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CursoControllerIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun criaEListaCurso() {
        mockMvc.perform(
            post("/academico/cursos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "nome": "Análise e Desenvolvimento de Sistemas",
                      "sigla": "TADS",
                      "codigo": "TADS-SEPT",
                      "horasFormativasMinimas": 120
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.sigla").value("TADS"))
            .andExpect(jsonPath("$._links.self").exists())

        mockMvc.perform(get("/academico/cursos"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].sigla").value("TADS"))
            .andExpect(jsonPath("$.page.totalElements").value(1))
    }
}
