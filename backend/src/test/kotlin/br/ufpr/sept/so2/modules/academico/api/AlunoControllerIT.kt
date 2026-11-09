package br.ufpr.sept.so2.modules.academico.api

import br.ufpr.sept.so2.modules.auditoria.infrastructure.persistence.AuditLogJpaRepository
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.persistence.OutboxEventJpaRepository
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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

    @Autowired
    private lateinit var outboxEventJpaRepository: OutboxEventJpaRepository

    @Autowired
    private lateinit var auditLogJpaRepository: AuditLogJpaRepository

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
    fun postCriaUsuarioComPrimeiroAcessoESemSenhaNaResposta() {
        val grr = grrUnico()
        val email = "it.aluno.conta.$grr@ufpr.br"
        val created = mockMvc.perform(
            post("/academico/alunos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(grr, email)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.senha").doesNotExist())
            .andExpect(jsonPath("$.senhaTemporaria").doesNotExist())
            .andExpect(jsonPath("$._links['reset-password']").exists())
            .andExpect(jsonPath("$._links.matricula").exists())
            .andReturn()
        val corpo = created.response.contentAsString
        val alunoId = ItJson.text(corpo, "id")
        val usuario = usuarioRepository.findByEmail(email).orElseThrow()
        assertFalse(usuario.senhaAlterada)
        assertTrue(usuario.senhaHash.contains("argon2"))
        val boasVindas = outboxEventJpaRepository.findAll().filter {
            it.tipo == "aluno.boas-vindas" && it.payload!!.contains(usuario.id.toString())
        }
        assertTrue(boasVindas.size == 1)
        val senha = ItJson.text(boasVindas.single().payload!!, "senhaTemporaria")
        assertFalse(corpo.contains(senha))
        val audit = auditLogJpaRepository.findAll().filter {
            it.tipo == "aluno.criado" && it.payload!!.contains(alunoId)
        }
        assertTrue(audit.size == 1)
        assertFalse(audit.single().payload!!.contains(senha))
        assertFalse(audit.single().payload!!.contains(grr))
    }

    @Test
    fun postVinculaUsuarioCompativelERejeitaParIncompativel() {
        val grr = grrUnico()
        val email = "it.aluno.vinculo.$grr@ufpr.br"
        usuariosIt.criarUsuario(email, grr, listOf("dashboard.view_own"))
        mockMvc.perform(
            post("/academico/alunos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(grr, email)),
        )
            .andExpect(status().isCreated)
        assertTrue(usuarioRepository.findByEmail(email).orElseThrow().senhaAlterada)

        val outroGrr = grrUnico()
        usuariosIt.criarUsuario("it.aluno.outro.$outroGrr@ufpr.br", outroGrr, listOf("dashboard.view_own"))
        mockMvc.perform(
            post("/academico/alunos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(outroGrr, "it.aluno.conflito.$outroGrr@ufpr.br")),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.type").value(containsString("conflict")))
    }

    @Test
    fun resetSenhaEnviaLinkDistintoSemSenhaEmClaro() {
        val grr = grrUnico()
        val email = "it.aluno.reset.$grr@ufpr.br"
        val created = mockMvc.perform(
            post("/academico/alunos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload(grr, email)),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val alunoId = ItJson.text(created.response.contentAsString, "id")
        val usuarioId = usuarioRepository.findByEmail(email).orElseThrow().id.toString()
        mockMvc.perform(
            post("/academico/alunos/$alunoId/reset-senha")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isAccepted)
        val eventos = outboxEventJpaRepository.findAll().filter {
            it.tipo == "PASSWORD_RESET_BY_ADMIN" && it.payload!!.contains(usuarioId)
        }
        assertTrue(eventos.isNotEmpty())
        val payload = eventos.last().payload!!
        assertTrue(payload.contains("/nova-senha?token="))
        assertFalse(payload.contains("senhaTemporaria"))
        val audit = auditLogJpaRepository.findAll().last { it.tipo == "user.password_reset" }
        assertTrue(audit.payload!!.contains("targetUserId"))
        assertTrue(audit.payload!!.contains("operadorId"))
        assertFalse(audit.payload!!.contains("senha"))
        assertFalse(audit.payload!!.contains("token"))
    }

    @Test
    fun matriculaSemVagaRetorna422ENaoGravaOLote() {
        garantirPeriodoVigente()
        val semVaga = criarDisciplina(0)
        val comVaga = criarDisciplina(3)
        mockMvc.perform(
            post("/academico/alunos/${criarAlunoValido()}/matriculas")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"disciplinaIds":["$comVaga","$semVaga"]}"""),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value(containsString("no-vacancies")))
            .andExpect(jsonPath("$.disciplinaId").value(semVaga))
        mockMvc.perform(get("/academico/disciplinas/$comVaga").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.vagas").value(3))
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

    private fun garantirPeriodoVigente() {
        val atual = mockMvc.perform(
            get("/academico/periodos/vigente").header("Authorization", "Bearer $token"),
        ).andReturn()
        if (atual.response.status == 200) {
            return
        }
        mockMvc.perform(
            post("/academico/periodos")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ano":2026,"semestre":2,"inicio":"2026-01-01","fim":"2026-12-20"}"""),
        )
            .andExpect(status().isCreated)
    }

    private fun criarDisciplina(vagas: Int): String {
        val codigo = "IT" + System.nanoTime().toString().takeLast(6)
        val created = mockMvc.perform(
            post("/academico/disciplinas")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "idCurso": "$cursoId",
                      "codigo": "$codigo",
                      "nome": "Disciplina $codigo",
                      "periodo": 1,
                      "cargaHorariaTotal": 60,
                      "creditos": 4,
                      "vagas": $vagas
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andReturn()
        return ItJson.text(created.response.contentAsString, "id")
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
