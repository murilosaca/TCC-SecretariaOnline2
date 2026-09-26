package br.ufpr.sept.so2.modules.bff.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.nullValue
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
class ProfessorDashboardControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        criarUsuarioSeAusente(
            "professor.bff@ufpr.br",
            "GRR20240113",
            listOf(
                "dashboard.view_self_professor",
                "event.manage",
                "event.host",
                "request.deliberate",
                "internship.review",
                "tcc.review",
            ),
            agora,
        )
        criarUsuarioSeAusente(
            "professor.caaf.bff@ufpr.br",
            "GRR20240114",
            listOf(
                "dashboard.view_self_professor",
                "event.manage",
                "request.deliberate",
                "formative.review",
            ),
            agora,
        )
        criarUsuarioSeAusente(
            "aluno.bff.prof@ufpr.br",
            "GRR20240115",
            listOf("dashboard.view_own", "request.view_own", "request.open", "attendance.view_open"),
            agora,
        )
        criarUsuarioSeAusente(
            "secretaria.bff.prof@ufpr.br",
            "GRR20240116",
            listOf("course.manage", "request.triage", "request.deliberate"),
            agora,
        )
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/bff/dashboard/professor"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun professorAutenticadoRecebePainelSemCaaf() {
        val token = login("professor.bff@ufpr.br")
        mockMvc.perform(
            get("/bff/dashboard/professor")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.saudacao.nome").exists())
            .andExpect(jsonPath("$.kpis.pendentesDeliberar").isNumber)
            .andExpect(jsonPath("$.kpis.eventosHoje").isNumber)
            .andExpect(jsonPath("$.kpis.slaUrgentes").isNumber)
            .andExpect(jsonPath("$.kpis.formativasRevisao").value(nullValue()))
            .andExpect(jsonPath("$.filaSolicitacoes").isArray)
            .andExpect(jsonPath("$.meusEventos").isArray)
            .andExpect(jsonPath("$.formativasCaaf").value(nullValue()))
            .andExpect(jsonPath("$.estagiosPendentes").isArray)
            .andExpect(jsonPath("$.tccsPendentes").isArray)
            .andExpect(jsonPath("$._links.self").value("/bff/dashboard/professor"))
            .andExpect(jsonPath("$._links.deliberar").value("/solicitacoes?to=me"))
            .andExpect(jsonPath("$._links.eventos").value("/professor/eventos"))
            .andExpect(jsonPath("$._links.formativasCaaf").doesNotExist())
            .andExpect(jsonPath("$._links.estagios").value("/estagios?to=me"))
            .andExpect(jsonPath("$._links.tccs").value("/tccs?to=me"))
    }

    @Test
    fun professorComFormativeReviewRecebeBlocoCaaf() {
        val token = login("professor.caaf.bff@ufpr.br")
        mockMvc.perform(
            get("/bff/dashboard/professor")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.kpis.formativasRevisao").isNumber)
            .andExpect(jsonPath("$.formativasCaaf").isArray)
            .andExpect(jsonPath("$._links.formativasCaaf").value("/formativas?to=me"))
    }

    @Test
    fun alunoRecebe403NoBffProfessor() {
        val token = login("aluno.bff.prof@ufpr.br")
        mockMvc.perform(
            get("/bff/dashboard/professor")
                .header("Authorization", "Bearer $token"),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun secretariaRecebe403NoBffProfessor() {
        val token = login("secretaria.bff.prof@ufpr.br")
        mockMvc.perform(
            get("/bff/dashboard/professor")
                .header("Authorization", "Bearer $token"),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun alunoMantemContratoDoBffAluno() {
        val token = login("aluno.bff.prof@ufpr.br")
        mockMvc.perform(
            get("/bff/dashboard/aluno")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.self").value("/bff/dashboard/aluno"))
    }

    private fun criarUsuarioSeAusente(
        email: String,
        grr: String,
        authorities: List<String>,
        agora: OffsetDateTime,
    ) {
        if (usuarioRepository.findByEmail(email).isPresent) {
            return
        }
        usuarioRepository.save(
            Usuario(
                Uuids.v7(),
                "Usuário IT Professor BFF",
                Email.of(email),
                null,
                Grr.of(grr),
                passwordHasher.hash("TroqueEstaSenha1!"),
                true,
                agora,
                "127.0.0.1",
                "it",
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
                .content("{\"identificador\":\"$email\",\"senha\":\"TroqueEstaSenha1!\"}"),
        )
            .andExpect(status().isOk)
            .andReturn()
        return extract(result.response.contentAsString, "\"accessToken\":\"", "\"")
    }

    companion object {
        private fun extract(json: String, startToken: String, endToken: String): String {
            val start = json.indexOf(startToken)
            val from = start + startToken.length
            val end = json.indexOf(endToken, from)
            return json.substring(from, end)
        }
    }
}
