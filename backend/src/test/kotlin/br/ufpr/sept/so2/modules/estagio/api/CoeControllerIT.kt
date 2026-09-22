package br.ufpr.sept.so2.modules.estagio.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.estagio.application.AtribuirEstagioCoeUseCase
import br.ufpr.sept.so2.modules.estagio.application.ports.CoeMembroPort
import br.ufpr.sept.so2.modules.estagio.application.ports.EstagioRepository
import br.ufpr.sept.so2.modules.estagio.domain.DocumentoEstagio
import br.ufpr.sept.so2.modules.estagio.domain.Estagio
import br.ufpr.sept.so2.modules.estagio.domain.TipoDocumentoEstagio
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
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
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoeControllerIT {

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
    private lateinit var estagioRepository: EstagioRepository

    @Autowired
    private lateinit var coeMembroPort: CoeMembroPort

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoId: UUID
    private lateinit var outroCursoId: UUID
    private lateinit var alunoId: UUID
    private lateinit var professorId: UUID
    private lateinit var colegaId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        cursoId = garantirCurso(CODIGO, "TADS COE IT", "TCOE", agora)
        outroCursoId = garantirCurso(CODIGO_OUTRO, "EC COE IT", "ECOE", agora)
        usuariosIt.criarUsuario(EMAIL_ALUNO, GRR_ALUNO, listOf("dashboard.view_own", "internship.view_own"))
        alunoId = criarAluno(cursoId, agora)
        professorId = usuariosIt.criarUsuario(
            EMAIL_PROFESSOR,
            GRR_PROFESSOR,
            listOf("dashboard.view_own", "internship.review"),
        ).id
        colegaId = usuariosIt.criarUsuario(
            EMAIL_COLEGA,
            GRR_COLEGA,
            listOf("dashboard.view_own", "internship.review"),
        ).id
        usuariosIt.criarUsuario(EMAIL_ALHEIO, GRR_ALHEIO, listOf("dashboard.view_own", "internship.review"))
        usuariosIt.criarUsuario(EMAIL_SEMCAP, GRR_SEMCAP, listOf("dashboard.view_own"))
        coeMembroPort.adicionarSeAusente(cursoId, professorId)
        coeMembroPort.adicionarSeAusente(cursoId, colegaId)
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/comissoes/coe"))
            .andExpect(status().isUnauthorized)
        mockMvc.perform(
            post("/comissoes/coe/atribuicoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"estagioId":"${Uuids.v7()}","assigneeId":"${Uuids.v7()}"}"""),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityRecebe403ENaoGanhaOMenu() {
        val token = usuariosIt.login(EMAIL_SEMCAP)
        mockMvc.perform(get("/comissoes/coe").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links['comissoes-coe']").doesNotExist())
            .andExpect(jsonPath("$._links['estagios-revisao']").doesNotExist())
    }

    @Test
    fun poolMostraSemOrientadorSelfAssignApareceNaFilaELoteNaoExiste() {
        val estagio = abrirEstagio(cursoId, null)
        val token = usuariosIt.login(EMAIL_PROFESSOR)

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links['comissoes-coe']").value("/comissoes/coe"))
            .andExpect(jsonPath("$._links['estagios-revisao']").value("/estagios?to=me"))

        mockMvc.perform(get("/comissoes/coe").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.kpis.poolTotal").value(1))
            .andExpect(jsonPath("$.kpis.atribuidosAMim").value(0))
            .andExpect(jsonPath("$.content[0].noPool").value(true))
            .andExpect(jsonPath("$.content[0]._links['assign-member']").value("/comissoes/coe/atribuicoes"))
            .andExpect(jsonPath("$._links['assign-member']").value("/comissoes/coe/atribuicoes"))

        mockMvc.perform(
            get("/estagios").param("canReview", "true").header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${estagio.id}')]").doesNotExist())

        val antes = contarOutbox()
        val auditsAntes = contarAudit()
        mockMvc.perform(
            post("/comissoes/coe/atribuicoes")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"estagioId":"${estagio.id}","assigneeId":"$professorId"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].comigo").value(true))
            .andExpect(jsonPath("$.kpis.atribuidosAMim").value(1))
            .andExpect(jsonPath("$.kpis.poolTotal").value(0))

        assertEquals(antes + 1, contarOutbox())
        assertEquals(auditsAntes + 1, contarAudit())

        mockMvc.perform(
            get("/estagios").param("canReview", "true").header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${estagio.id}')].empresa").value(EMPRESA))

        mockMvc.perform(get("/comissoes/coe/aprovacoes").header("Authorization", "Bearer $token"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun atribuirAColegaSomeDoPoolENaoEnumeraOutroCurso() {
        val meuEstagio = abrirEstagio(cursoId, null)
        val fora = abrirEstagio(outroCursoId, null)
        val token = usuariosIt.login(EMAIL_PROFESSOR)

        mockMvc.perform(
            post("/comissoes/coe/atribuicoes")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"estagioId":"${meuEstagio.id}","assigneeId":"$colegaId"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${meuEstagio.id}')]").doesNotExist())

        mockMvc.perform(
            get("/estagios").param("canReview", "true").header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${meuEstagio.id}')]").doesNotExist())

        val tokenColega = usuariosIt.login(EMAIL_COLEGA)
        mockMvc.perform(
            get("/estagios").param("canReview", "true").header("Authorization", "Bearer $tokenColega"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${meuEstagio.id}')].empresa").value(EMPRESA))

        mockMvc.perform(
            post("/comissoes/coe/atribuicoes")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"estagioId":"${fora.id}","assigneeId":"$professorId"}"""),
        )
            .andExpect(status().isNotFound)

        mockMvc.perform(
            post("/comissoes/coe/atribuicoes")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"estagioId":"${Uuids.v7()}","assigneeId":"$professorId"}"""),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun membroAlheioNaoVeEstagioDeOutroCurso() {
        val estagio = abrirEstagio(cursoId, null)
        val token = usuariosIt.login(EMAIL_ALHEIO)
        mockMvc.perform(get("/comissoes/coe").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isEmpty)
            .andExpect(jsonPath("$.kpis.poolTotal").value(0))
        mockMvc.perform(
            post("/comissoes/coe/atribuicoes")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"estagioId":"${estagio.id}","assigneeId":"$professorId"}"""),
        )
            .andExpect(status().isNotFound)
    }

    private fun abrirEstagio(idCurso: UUID, orientadorId: UUID?): Estagio {
        val agora = OffsetDateTime.now()
        return estagioRepository.save(
            Estagio.abrir(
                Uuids.v7(),
                alunoId,
                idCurso,
                orientadorId,
                EMPRESA,
                "Ana Supervisora",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 12, 15),
                agora,
                listOf(
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.TCE),
                    DocumentoEstagio.pendente(Uuids.v7(), TipoDocumentoEstagio.RELATORIO_FINAL),
                ),
            ),
        )
    }

    private fun garantirCurso(codigo: String, nome: String, sigla: String, agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(Curso(Uuids.v7(), nome, sigla, codigo, null, 120, true, agora, agora)).id
    }

    private fun criarAluno(idCurso: UUID, agora: OffsetDateTime): UUID {
        val existente = alunoRepository.findByGrr(GRR_ALUNO)
        if (existente.isPresent) {
            return existente.get().id
        }
        return alunoRepository.save(
            Aluno(
                Uuids.v7(),
                "Aluno COE IT",
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

    private fun contarOutbox(): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from outbox_event where tipo = ?",
            Int::class.java,
            AtribuirEstagioCoeUseCase.TIPO,
        ) ?: 0

    private fun contarAudit(): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from audit_log where tipo = ?",
            Int::class.java,
            AtribuirEstagioCoeUseCase.TIPO,
        ) ?: 0

    companion object {
        private const val EMAIL_ALUNO = "it.coe.aluno@ufpr.br"
        private const val EMAIL_PROFESSOR = "it.coe.prof@ufpr.br"
        private const val EMAIL_COLEGA = "it.coe.colega@ufpr.br"
        private const val EMAIL_ALHEIO = "it.coe.alheio@ufpr.br"
        private const val EMAIL_SEMCAP = "it.coe.semcap@ufpr.br"
        private const val GRR_ALUNO = "GRR20249201"
        private const val GRR_PROFESSOR = "GRR20249202"
        private const val GRR_COLEGA = "GRR20249203"
        private const val GRR_ALHEIO = "GRR20249204"
        private const val GRR_SEMCAP = "GRR20249205"
        private const val CODIGO = "TADS-COE-IT"
        private const val CODIGO_OUTRO = "EC-COE-IT"
        private const val EMPRESA = "Empresa Pool COE IT"
    }
}
