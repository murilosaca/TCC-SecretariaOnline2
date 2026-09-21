package br.ufpr.sept.so2.modules.academico.api

import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlunoControllerIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var token: String
    private lateinit var cursoId: String

    @BeforeEach
    fun seed() {
        usuariosIt.criarUsuario(EMAIL_SEC, "GRR20247201", AUTHORITIES_SEC)
        token = usuariosIt.login(EMAIL_SEC)
        val (sigla, codigo) = codigoUnico("AL")
        val created = mockMvc.perform(
            post("/academico/cursos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "nome": "Curso validação aluno",
                      "sigla": "$sigla",
                      "codigo": "$codigo",
                      "horasFormativasMinimas": 120,
                      "secretariosIds": []
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andReturn()
        cursoId = ItJson.text(created.response.contentAsString, "id")
    }

    @Test
    fun postComGrrValidoRetorna201() {
        val grr = grrUnico()
        val email = "it.aluno.ok.$grr@ufpr.br"
        mockMvc.perform(
            post("/academico/alunos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(grr, email)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.grr").value(grr))
    }

    @Test
    fun postSemGrrRetorna422() {
        mockMvc.perform(
            post("/academico/alunos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(grr = "", email = "it.aluno.blank@ufpr.br")),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value(containsString("validation-error")))
            .andExpect(jsonPath("$.erros[*].campo").value(hasItem("grr")))
    }

    @Test
    fun postComGrrMalformadoRetorna422() {
        mockMvc.perform(
            post("/academico/alunos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("GRR123", "it.aluno.grr.ruim@ufpr.br")),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value(containsString("validation-error")))
            .andExpect(jsonPath("$.erros[*].campo").value(hasItem("grr")))
    }

    @Test
    fun postComEmailMalformadoRetorna422() {
        mockMvc.perform(
            post("/academico/alunos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(grrUnico(), "nao-e-email")),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value(containsString("validation-error")))
            .andExpect(jsonPath("$.erros[*].campo").value(hasItem("emailInstitucional")))
    }

    @Test
    fun putComGrrMalformadoRetorna422() {
        val alunoId = criarAlunoValido()
        mockMvc.perform(
            put("/academico/alunos/$alunoId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload("GRR123", "it.aluno.put.grr@ufpr.br")),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value(containsString("validation-error")))
            .andExpect(jsonPath("$.erros[*].campo").value(hasItem("grr")))
    }

    @Test
    fun putComEmailMalformadoRetorna422() {
        val alunoId = criarAlunoValido()
        mockMvc.perform(
            put("/academico/alunos/$alunoId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(grrUnico(), "nao-e-email")),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value(containsString("validation-error")))
            .andExpect(jsonPath("$.erros[*].campo").value(hasItem("emailInstitucional")))
    }

    private fun criarAlunoValido(): String {
        val grr = grrUnico()
        val created = mockMvc.perform(
            post("/academico/alunos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(grr, "it.aluno.put.$grr@ufpr.br")),
        )
            .andExpect(status().isCreated)
            .andReturn()
        return ItJson.text(created.response.contentAsString, "id")
    }

    private fun payload(grr: String, email: String): String =
        """
        {
          "nome": "Aluno IT",
          "grr": "$grr",
          "emailInstitucional": "$email",
          "emailPessoal": "",
          "idCurso": "$cursoId",
          "situacao": "MATRICULADO"
        }
        """.trimIndent()

    companion object {
        private const val EMAIL_SEC = "it.aluno.sec@ufpr.br"
        private val AUTHORITIES_SEC = listOf(
            "course.manage",
            "subject.manage",
            "user.manage_students",
            "calendar.manage",
        )

        private fun codigoUnico(prefixo: String): Pair<String, String> {
            val n = System.nanoTime().toString().takeLast(8)
            return prefixo.take(2) + n.take(4) to "$prefixo-$n"
        }

        private fun grrUnico(): String {
            val n = (System.nanoTime() % 100_000_000).toString().padStart(8, '0')
            return "GRR$n"
        }
    }
}
