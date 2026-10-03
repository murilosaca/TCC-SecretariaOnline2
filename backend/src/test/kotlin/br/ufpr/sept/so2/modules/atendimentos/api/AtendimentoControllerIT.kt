package br.ufpr.sept.so2.modules.atendimentos.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.atendimentos.application.ports.CategoriaAtendimentoRepository
import br.ufpr.sept.so2.modules.atendimentos.domain.CategoriaAtendimento
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AtendimentoControllerIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var cursoRepository: CursoRepository

    @Autowired
    private lateinit var cursoSecretarioRepository: CursoSecretarioRepository

    @Autowired
    private lateinit var alunoRepository: AlunoRepository

    @Autowired
    private lateinit var categoriaRepository: CategoriaAtendimentoRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoId: UUID
    private lateinit var cursoForaId: UUID
    private lateinit var alunoId: UUID
    private lateinit var alunoForaId: UUID
    private lateinit var categoriaId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.parse("2026-10-03T12:00:00Z")
        cursoId = garantirCurso(CODIGO, "TADS Atend IT", "ATIT", agora)
        cursoForaId = garantirCurso(CODIGO_FORA, "Outro Atend IT", "OAIT", agora)
        val secretaria = usuariosIt.criarUsuario(
            EMAIL_SEC,
            GRR_SEC,
            listOf("service_record.create", "user.manage_students"),
        )
        cursoSecretarioRepository.adicionarSeAusente(cursoId, secretaria.id)
        usuariosIt.criarUsuario(EMAIL_SEM_CAP, GRR_SEM_CAP, listOf("course.manage"))
        usuariosIt.criarUsuario(EMAIL_ALUNO, GRR_ALUNO, listOf("service_record.view_own"))
        usuariosIt.criarUsuario(EMAIL_OUTRO, GRR_OUTRO, listOf("service_record.view_own"))
        alunoId = garantirAluno("Aluno Atend", GRR_ALUNO, EMAIL_ALUNO, cursoId, agora)
        alunoForaId = garantirAluno("Aluno Fora", GRR_OUTRO, EMAIL_OUTRO, cursoForaId, agora)
        categoriaId = garantirCategoria(agora)
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/atendimentos").param("aluno", "me"))
            .andExpect(status().isUnauthorized)
        mockMvc.perform(multipart("/atendimentos").param("assunto", "x"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityRecebe403() {
        val token = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(
            multipart("/atendimentos")
                .param("alunoId", alunoId.toString())
                .param("categoriaId", categoriaId.toString())
                .param("assunto", "Guichê")
                .param("resposta", "Resolvido")
                .header("Authorization", "Bearer $token"),
        ).andExpect(status().isForbidden)
        mockMvc.perform(get("/atendimentos").param("aluno", "me").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun alunoForaDoEscopoEInexistenteRecebemOMesmo404() {
        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(
            multipart("/atendimentos")
                .param("alunoId", alunoForaId.toString())
                .param("categoriaId", categoriaId.toString())
                .param("assunto", "Guichê")
                .param("resposta", "Resolvido")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.detail").value("Aluno não encontrado."))
        mockMvc.perform(
            multipart("/atendimentos")
                .param("alunoId", UUID.randomUUID().toString())
                .param("categoriaId", categoriaId.toString())
                .param("assunto", "Guichê")
                .param("resposta", "Resolvido")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.detail").value("Aluno não encontrado."))
    }

    @Test
    fun semAssuntoOuRespostaOuArquivoInvalidoRecebe422() {
        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(
            multipart("/atendimentos")
                .param("alunoId", alunoId.toString())
                .param("categoriaId", categoriaId.toString())
                .param("resposta", "Resolvido")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value("https://secretariaonline.ufpr.br/errors/validation-error"))
        mockMvc.perform(
            multipart("/atendimentos")
                .param("alunoId", alunoId.toString())
                .param("categoriaId", categoriaId.toString())
                .param("assunto", "Guichê")
                .header("Authorization", "Bearer $token"),
        ).andExpect(status().isUnprocessableEntity)
        mockMvc.perform(
            multipart("/atendimentos")
                .file(MockMultipartFile("anexo", "nota.txt", "text/plain", "nao e pdf".toByteArray()))
                .param("alunoId", alunoId.toString())
                .param("categoriaId", categoriaId.toString())
                .param("assunto", "Guichê")
                .param("resposta", "Resolvido")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.detail").value("Envie um PDF de até 10 MB."))
    }

    @Test
    fun registroEAuditLogSaemNaMesmaTransacao() {
        val token = usuariosIt.login(EMAIL_SEC)
        val outbox = contar("outbox_event", "atendimento.registrado")
        val audit = contar("audit_log", "atendimento.registrado")
        val criado = mockMvc.perform(
            multipart("/atendimentos")
                .param("alunoId", alunoId.toString())
                .param("categoriaId", categoriaId.toString())
                .param("assunto", "Documentação")
                .param("resposta", "Protocolo emitido no guichê.")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.estado").value("PENDENTE_CIENCIA"))
            .andExpect(jsonPath("$._links.acknowledge").doesNotExist())
            .andReturn()
        val id = ItJson.text(criado.response.contentAsString, "id")
        assertEquals(outbox + 1, contar("outbox_event", "atendimento.registrado"))
        assertEquals(audit + 1, contar("audit_log", "atendimento.registrado"))

        val aluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(get("/atendimentos").param("aluno", "me").header("Authorization", "Bearer $aluno"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='$id')]._links.acknowledge").value("/atendimentos/$id/acknowledge"))
            .andExpect(jsonPath("$._links.novo").doesNotExist())
    }

    @Test
    fun cienciaDeOutroAlunoRecebe404ESegundaCiencia409() {
        val secretaria = usuariosIt.login(EMAIL_SEC)
        val criado = mockMvc.perform(
            multipart("/atendimentos")
                .param("alunoId", alunoId.toString())
                .param("categoriaId", categoriaId.toString())
                .param("assunto", "Ciência")
                .param("resposta", "Leia o termo.")
                .header("Authorization", "Bearer $secretaria"),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val id = ItJson.text(criado.response.contentAsString, "id")
        val audit = contar("audit_log", "atendimentos.acknowledged")

        val outro = usuariosIt.login(EMAIL_OUTRO)
        mockMvc.perform(
            post("/atendimentos/$id/acknowledge").header("Authorization", "Bearer $outro"),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.detail").value("Atendimento não encontrado."))
        assertEquals(audit, contar("audit_log", "atendimentos.acknowledged"))

        val aluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(
            post("/atendimentos/$id/acknowledge")
                .header("Authorization", "Bearer $aluno")
                .header("X-Forwarded-For", "203.0.113.10"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("CIENCIA_DADA"))
            .andExpect(jsonPath("$._links.acknowledge").doesNotExist())
        assertEquals(audit + 1, contar("audit_log", "atendimentos.acknowledged"))

        mockMvc.perform(
            post("/atendimentos/$id/acknowledge").header("Authorization", "Bearer $aluno"),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.type").value("https://secretariaonline.ufpr.br/errors/conflict"))
    }

    private fun garantirCurso(codigo: String, nome: String, sigla: String, agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(Curso(Uuids.v7(), nome, sigla, codigo, null, 120, true, agora, agora)).id
    }

    private fun garantirAluno(
        nome: String,
        grr: String,
        email: String,
        idCurso: UUID,
        agora: OffsetDateTime,
    ): UUID {
        val existente = alunoRepository.findByGrr(grr)
        if (existente.isPresent) {
            return existente.get().id
        }
        return alunoRepository.save(
            Aluno(
                Uuids.v7(),
                nome,
                null,
                Grr.of(grr),
                Email.of(email),
                null,
                null,
                idCurso,
                AlunoSituacao.MATRICULADO,
                true,
                agora,
                agora,
            ),
        ).id
    }

    private fun garantirCategoria(agora: OffsetDateTime): UUID {
        val existente = categoriaRepository.findByNome("Documentação")
        if (existente != null) {
            return existente.id
        }
        return categoriaRepository.save(CategoriaAtendimento(Uuids.v7(), "Documentação", true, agora, agora)).id
    }

    private fun contar(tabela: String, tipo: String): Int =
        jdbcTemplate.queryForObject("select count(*) from $tabela where tipo = ?", Int::class.java, tipo) ?: 0

    companion object {
        private const val EMAIL_SEC = "it.atend.sec@ufpr.br"
        private const val EMAIL_SEM_CAP = "it.atend.semcap@ufpr.br"
        private const val EMAIL_ALUNO = "it.atend.aluno@ufpr.br"
        private const val EMAIL_OUTRO = "it.atend.outro@ufpr.br"
        private const val GRR_SEC = "GRR20249501"
        private const val GRR_SEM_CAP = "GRR20249502"
        private const val GRR_ALUNO = "GRR20249503"
        private const val GRR_OUTRO = "GRR20249504"
        private const val CODIGO = "ATEND-IT"
        private const val CODIGO_FORA = "ATEND-FORA"
    }
}
