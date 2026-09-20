package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.infrastructure.Uuids
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
class AuthMeLinksIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        criarUsuario(
            EMAIL_SEC,
            "GRR20247131",
            listOf(
                "course.manage",
                "subject.manage",
                "user.manage_students",
                "calendar.manage",
                "request.view_curso",
                "request.triage",
                "request.deliberate",
            ),
            agora,
        )
        criarUsuario(
            EMAIL_ALUNO,
            "GRR20247132",
            listOf(
                "dashboard.view_own",
                "request.view_own",
                "request.open",
                "attendance.view_open",
                "formative.view_own",
                "certificate.view_own",
            ),
            agora,
        )
    }

    @Test
    fun meSecretariaTemCursosENaoTemRevisaoCaaf() {
        val token = login(EMAIL_SEC)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.inicio").value("/inicio"))
            .andExpect(jsonPath("$._links.cursos").value("/secretaria/cursos"))
            .andExpect(jsonPath("$._links.alunos").value("/secretaria/alunos"))
            .andExpect(jsonPath("$._links.deliberar").value("/solicitacoes?to=me"))
            .andExpect(jsonPath("$._links.solicitacoes").doesNotExist())
            .andExpect(jsonPath("$._links.contato").value("/contato"))
            .andExpect(jsonPath("$._links['revisao-caaf']").doesNotExist())
            .andExpect(jsonPath("$._links.formativas").doesNotExist())
    }

    @Test
    fun meAlunoTemFormativasENaoTemCursos() {
        val token = login(EMAIL_ALUNO)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.formativas").value("/formativas"))
            .andExpect(jsonPath("$._links.certificados").value("/certificados"))
            .andExpect(jsonPath("$._links.cursos").doesNotExist())
            .andExpect(jsonPath("$._links.deliberar").doesNotExist())
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
        private const val EMAIL_SEC = "it.fgac.me.sec@ufpr.br"
        private const val EMAIL_ALUNO = "it.fgac.me.aluno@ufpr.br"
    }
}
