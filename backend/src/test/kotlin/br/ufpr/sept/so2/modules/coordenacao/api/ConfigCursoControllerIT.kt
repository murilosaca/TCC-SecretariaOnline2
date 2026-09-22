package br.ufpr.sept.so2.modules.coordenacao.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import br.ufpr.sept.so2.modules.formativas.domain.FormativaOrigem
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConfigCursoControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var cursoRepository: CursoRepository

    @Autowired
    private lateinit var alunoRepository: AlunoRepository

    @Autowired
    private lateinit var formativaRepository: FormativaRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoTadsId: UUID
    private lateinit var cursoEcId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.parse("2026-06-01T12:00:00Z")
        val coord = usuariosIt.criarUsuario(EMAIL_COORD, GRR_COORD, listOf("course.config", "tcc.review"))
        val outroCoord = usuariosIt.criarUsuario(EMAIL_OUTRO, GRR_OUTRO, listOf("course.config"))
        usuariosIt.criarUsuario(EMAIL_SECRETARIA, GRR_SECRETARIA, listOf("course.manage"))
        usuariosIt.criarUsuario(
            EMAIL_ALUNO,
            GRR_ALUNO,
            listOf("dashboard.view_own", "request.view_own", "attendance.view_open"),
        )
        cursoTadsId = garantirCurso(CODIGO_TADS, "TADS F61", "T6F", coord.id, agora)
        cursoEcId = garantirCurso(CODIGO_EC, "EC F61", "ECF", outroCoord.id, agora)
        val alunoId = garantirAluno(cursoTadsId, agora)
        garantirFormativa(alunoId, agora)
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/coordenacao/cursos/$cursoTadsId/config"))
            .andExpect(status().isUnauthorized)
        mockMvc.perform(
            patch("/coordenacao/cursos/$cursoTadsId/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"horasFormativasMinimas":150}"""),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun secretariaSemCapRecebe403ENaoGanhaATela() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        mockMvc.perform(get("/coordenacao/cursos/$cursoTadsId/config").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.cursos").value("/secretaria/cursos"))
            .andExpect(jsonPath("$._links['configurar-curso']").doesNotExist())
    }

    @Test
    fun coordenadorLeESalvaOProprioCursoSemInvalidarQuemJaEraElegivel() {
        val token = usuariosIt.login(EMAIL_COORD)
        val auditsAntes = contarAudit()

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links['configurar-curso']").value("/coordenacao/cursos/$cursoTadsId/configurar"))
            .andExpect(jsonPath("$._links.cursos").doesNotExist())

        mockMvc.perform(get("/coordenacao/cursos/$cursoTadsId/config").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.horasFormativasMinimas").value(120))
            .andExpect(jsonPath("$.duracaoCalendario").value(15))
            .andExpect(jsonPath("$.bancaMembrosExternos").value(1))
            .andExpect(jsonPath("$.bancaModalidade").value("PRESENCIAL"))
            .andExpect(jsonPath("$._links.update").value("/coordenacao/cursos/$cursoTadsId/config"))

        mockMvc.perform(
            patch("/coordenacao/cursos/$cursoTadsId/config")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"horasFormativasMinimas":150}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.horasFormativasMinimas").value(150))
            .andExpect(jsonPath("$._links.update").exists())

        assertEquals(150, cursoRepository.findById(cursoTadsId).get().horasFormativasMinimas)
        assertTrue(contarAudit() > auditsAntes)
        assertEquals(1, contarElegiveis(cursoTadsId))

        val aluno = usuariosIt.login(EMAIL_ALUNO)
        mockMvc.perform(get("/bff/dashboard/aluno").header("Authorization", "Bearer $aluno"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.kpis.horasFormativas.validadas").value(125))
            .andExpect(jsonPath("$.kpis.horasFormativas.requeridas").value(120))
    }

    @Test
    fun outroCursoDevolve403SemVazarDados() {
        val token = usuariosIt.login(EMAIL_COORD)
        mockMvc.perform(get("/coordenacao/cursos/$cursoEcId/config").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Você não é coordenador deste curso."))
            .andExpect(jsonPath("$.nome").doesNotExist())
            .andExpect(jsonPath("$.horasFormativasMinimas").doesNotExist())
        mockMvc.perform(
            patch("/coordenacao/cursos/$cursoEcId/config")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"horasFormativasMinimas":180}"""),
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Você não é coordenador deste curso."))
        assertEquals(120, cursoRepository.findById(cursoEcId).get().horasFormativasMinimas)
    }

    @Test
    fun horasInvalidasDevolvem422() {
        val token = usuariosIt.login(EMAIL_COORD)
        mockMvc.perform(
            patch("/coordenacao/cursos/$cursoTadsId/config")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"horasFormativasMinimas":-10}"""),
        )
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.type").value("https://secretariaonline.ufpr.br/errors/validation-error"))
            .andExpect(jsonPath("$.detail").value("Valor deve ser entre 0 e 1000"))
    }

    private fun garantirCurso(
        codigo: String,
        nome: String,
        sigla: String,
        coordenadorId: UUID,
        agora: OffsetDateTime,
    ): UUID {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            val curso = existente.get()
            curso.atualizar(nome, sigla, codigo, coordenadorId, 120, true)
            return cursoRepository.save(curso).id
        }
        return cursoRepository.save(
            Curso(Uuids.v7(), nome, sigla, codigo, coordenadorId, 120, true, agora, agora),
        ).id
    }

    private fun garantirAluno(idCurso: UUID, agora: OffsetDateTime): UUID {
        val existente = alunoRepository.findByGrr(GRR_ALUNO)
        if (existente.isPresent) {
            val aluno = existente.get()
            aluno.atualizar(null, null, null, null, idCurso, AlunoSituacao.MATRICULADO, true)
            return alunoRepository.save(aluno).id
        }
        return alunoRepository.save(
            Aluno(
                Uuids.v7(),
                "João F61",
                null,
                Grr.of(GRR_ALUNO),
                Email.of(EMAIL_ALUNO),
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

    private fun garantirFormativa(alunoId: UUID, agora: OffsetDateTime) {
        val eventoId = UUID.fromString("01999999-0000-7000-8000-00000000f061")
        val existente = formativaRepository.findByEventoAndAluno(eventoId, alunoId)
        if (existente != null) {
            return
        }
        formativaRepository.save(
            Formativa(
                Uuids.v7(),
                alunoId,
                eventoId,
                FormativaOrigem.PRESENCA_VALIDADA,
                "Oficina F61",
                125,
                FormativaEstado.APROVADA,
                agora,
                agora,
            ),
        )
    }

    private fun contarAudit(): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from audit_log where tipo = 'curso.config.atualizada'",
            Int::class.java,
        ) ?: 0

    private fun contarElegiveis(cursoId: UUID): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from elegibilidade_horas where id_curso = ? and elegivel = true",
            Int::class.java,
            cursoId,
        ) ?: 0

    companion object {
        private const val EMAIL_COORD = "coord.f61@ufpr.br"
        private const val GRR_COORD = "GRR20246101"
        private const val EMAIL_OUTRO = "coord.ec.f61@ufpr.br"
        private const val GRR_OUTRO = "GRR20246102"
        private const val EMAIL_SECRETARIA = "secretaria.f61@ufpr.br"
        private const val GRR_SECRETARIA = "GRR20246103"
        private const val EMAIL_ALUNO = "aluno.f61@ufpr.br"
        private const val GRR_ALUNO = "GRR20246104"
        private const val CODIGO_TADS = "TADS-F61-IT"
        private const val CODIGO_EC = "EC-F61-IT"
    }
}
