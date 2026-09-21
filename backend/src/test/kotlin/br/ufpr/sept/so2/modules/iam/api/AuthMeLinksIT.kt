package br.ufpr.sept.so2.modules.iam.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        usuariosIt.criarUsuario(
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
        )
        usuariosIt.criarUsuario(
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
        )
    }

    @Test
    fun meSecretariaTemCursosENaoTemRevisaoCaaf() {
        val token = usuariosIt.login(EMAIL_SEC)
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
        val token = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.formativas").value("/formativas"))
            .andExpect(jsonPath("$._links.certificados").value("/certificados"))
            .andExpect(jsonPath("$._links.cursos").doesNotExist())
            .andExpect(jsonPath("$._links.deliberar").doesNotExist())
    }

    companion object {
        private const val EMAIL_SEC = "it.fgac.me.sec@ufpr.br"
        private const val EMAIL_ALUNO = "it.fgac.me.aluno@ufpr.br"
    }
}
