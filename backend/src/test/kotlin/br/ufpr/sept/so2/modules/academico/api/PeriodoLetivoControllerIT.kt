package br.ufpr.sept.so2.modules.academico.api

import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.junit.jupiter.api.AfterEach
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
class PeriodoLetivoControllerIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var periodoLetivoRepository: PeriodoLetivoRepository

    @BeforeEach
    fun seed() {
        limparPeriodosDaClasse()
        val agora = OffsetDateTime.now()
        criarUsuario(
            EMAIL_SEC,
            "GRR20247111",
            listOf("calendar.manage"),
            agora,
        )
        criarUsuario(
            EMAIL_ALUNO,
            "GRR20247112",
            listOf("dashboard.view_own", "request.view_own"),
            agora,
        )
    }

    @AfterEach
    fun limpar() {
        limparPeriodosDaClasse()
    }

    @Test
    fun semBearerRetorna401() {
        mockMvc.perform(get("/academico/periodos"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.type").value(org.hamcrest.Matchers.containsString("authentication-required")))
    }

    @Test
    fun alunoRecebe403() {
        val token = login(EMAIL_ALUNO)
        mockMvc.perform(get("/academico/periodos").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.type").value(org.hamcrest.Matchers.containsString("access-denied")))
    }

    @Test
    fun criaListaERecusaSobreposicao() {
        val token = login(EMAIL_SEC)
        mockMvc.perform(
            post("/academico/periodos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadPeriodo(ANO_ISOLADO, 1, "$ANO_ISOLADO-02-01", "$ANO_ISOLADO-07-15")),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.semestre").value(1))
            .andExpect(jsonPath("$._links.self").exists())
            .andExpect(jsonPath("$._links.atualizar").exists())

        mockMvc.perform(
            post("/academico/periodos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadPeriodo(ANO_ISOLADO, 2, "$ANO_ISOLADO-07-01", "$ANO_ISOLADO-12-20")),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value(org.hamcrest.Matchers.containsString("validation-error")))
            .andExpect(jsonPath("$.detail").value("Período sobrepõe $ANO_ISOLADO/1"))

        mockMvc.perform(get("/academico/periodos").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.criar").value("/academico/periodos"))
            .andExpect(jsonPath("$.content[*].ano").value(org.hamcrest.Matchers.hasItem(ANO_ISOLADO)))
    }

    private fun limparPeriodosDaClasse() {
        periodoLetivoRepository.findAll()
            .filter { it.ano == ANO_ISOLADO }
            .forEach { periodoLetivoRepository.deleteById(it.id) }
    }

    private fun criarUsuario(email: String, grr: String, authorities: List<String>, agora: OffsetDateTime) {
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
                .content("{\"identificador\":\"$email\",\"senha\":\"$SENHA\"}"),
        )
            .andExpect(status().isOk)
            .andReturn()
        return ItJson.text(result.response.contentAsString, "accessToken")
    }

    companion object {
        private const val SENHA = "TroqueEstaSenha1!"
        private const val EMAIL_SEC = "it.fgac.cal@ufpr.br"
        private const val EMAIL_ALUNO = "it.fgac.cal.aluno@ufpr.br"
        private const val ANO_ISOLADO = 2088

        private fun payloadPeriodo(ano: Int, semestre: Int, inicio: String, fim: String): String =
            """
            {
              "ano": $ano,
              "semestre": $semestre,
              "inicio": "$inicio",
              "fim": "$fim"
            }
            """.trimIndent()
    }
}
