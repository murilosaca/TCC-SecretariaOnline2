package br.ufpr.sept.so2.modules.formativas.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.formativas.application.ports.ComissaoMembroPort
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.TipoComissao
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
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CaafControllerIT {

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
    private lateinit var comissaoMembroPort: ComissaoMembroPort

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoId: UUID
    private lateinit var outroCursoId: UUID
    private lateinit var alunoId: UUID
    private lateinit var caafId: UUID
    private lateinit var colegaId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        cursoId = garantirCurso(CODIGO, "TADS CAAF IT", "TCAF", agora)
        outroCursoId = garantirCurso(CODIGO_OUTRO, "EC CAAF IT", "ECAF", agora)
        usuariosIt.criarUsuario(EMAIL_ALUNO, GRR_ALUNO, listOf("dashboard.view_own", "formative.view_own"))
        alunoId = criarAluno(cursoId, agora)
        caafId = usuariosIt.criarUsuario(
            EMAIL_CAAF,
            GRR_CAAF,
            listOf("dashboard.view_own", "formative.review"),
        ).id
        colegaId = usuariosIt.criarUsuario(
            EMAIL_COLEGA,
            GRR_COLEGA,
            listOf("dashboard.view_own", "formative.review"),
        ).id
        usuariosIt.criarUsuario(EMAIL_ALHEIO, GRR_ALHEIO, listOf("dashboard.view_own", "formative.review"))
        usuariosIt.criarUsuario(EMAIL_SEMCAP, GRR_SEMCAP, listOf("dashboard.view_own"))
        comissaoMembroPort.adicionarSeAusente(cursoId, caafId, TipoComissao.CAAF)
        comissaoMembroPort.adicionarSeAusente(cursoId, colegaId, TipoComissao.CAAF)
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/comissoes/caaf"))
            .andExpect(status().isUnauthorized)
        mockMvc.perform(
            post("/comissoes/caaf/atribuicoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"formativaId":"${Uuids.v7()}","assigneeId":"${Uuids.v7()}"}"""),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityRecebe403ENaoGanhaOMenu() {
        val token = usuariosIt.login(EMAIL_SEMCAP)
        mockMvc.perform(get("/comissoes/caaf").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links['comissoes-caaf']").doesNotExist())
            .andExpect(jsonPath("$._links['revisao-caaf']").doesNotExist())
    }

    @Test
    fun poolSelfAssignELoteComPresencaValidada() {
        val formativa = abrirFormativa()
        val token = usuariosIt.login(EMAIL_CAAF)

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links['comissoes-caaf']").value("/comissoes/caaf"))
            .andExpect(jsonPath("$._links['revisao-caaf']").value("/formativas?to=me"))

        mockMvc.perform(get("/comissoes/caaf").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.kpis.poolTotal").value(1))
            .andExpect(jsonPath("$.kpis.atribuidasAMim").value(0))
            .andExpect(jsonPath("$.content[0].noPool").value(true))
            .andExpect(jsonPath("$.content[0].elegivelLote").value(true))
            .andExpect(jsonPath("$.content[0]._links['assign-member']").value("/comissoes/caaf/atribuicoes"))
            .andExpect(jsonPath("$.content[0]._links['batch-approve']").value("/comissoes/caaf/lote"))
            .andExpect(jsonPath("$._links['assign-member']").value("/comissoes/caaf/atribuicoes"))
            .andExpect(jsonPath("$._links['batch-approve']").value("/comissoes/caaf/lote"))

        mockMvc.perform(
            post("/comissoes/caaf/atribuicoes")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"formativaId":"${formativa.id}","assigneeId":"$caafId"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].comigo").value(true))
            .andExpect(jsonPath("$.kpis.atribuidasAMim").value(1))
            .andExpect(jsonPath("$.kpis.poolTotal").value(0))

        val outboxAntes = contarOutbox()
        mockMvc.perform(
            post("/comissoes/caaf/lote")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ids":["${formativa.id}"],"decisao":"APROVADA"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isEmpty)
            .andExpect(jsonPath("$.kpis.aprovadasNoPeriodo").value(1))

        assertEquals(outboxAntes + 3, contarOutbox())
    }

    @Test
    fun atribuirAColegaSomeDoPoolENaoEnumeraOutroCurso() {
        val minha = abrirFormativa()
        val fora = abrirFormativaOutroCurso()
        val token = usuariosIt.login(EMAIL_CAAF)

        mockMvc.perform(
            post("/comissoes/caaf/atribuicoes")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"formativaId":"${minha.id}","assigneeId":"$colegaId"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[?(@.id=='${minha.id}')]").doesNotExist())

        mockMvc.perform(
            post("/comissoes/caaf/atribuicoes")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"formativaId":"${fora.id}","assigneeId":"$caafId"}"""),
        )
            .andExpect(status().isNotFound)

        mockMvc.perform(
            post("/comissoes/caaf/lote")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ids":["${fora.id}"],"decisao":"APROVADA"}"""),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun indeferirEmLoteRecebe422() {
        val formativa = abrirFormativa()
        val token = usuariosIt.login(EMAIL_CAAF)
        mockMvc.perform(
            post("/comissoes/caaf/lote")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"ids":["${formativa.id}"],"decisao":"INDEFERIDA"}"""),
        )
            .andExpect(status().isUnprocessableEntity)
    }

    private fun abrirFormativa(): Formativa {
        val agora = OffsetDateTime.now()
        val formativa = Formativa.viaPresenca(
            Uuids.v7(),
            alunoId,
            Uuids.v7(),
            "Pool CAAF IT",
            4,
            agora,
        )
        formativa.confirmar(agora)
        return formativaRepository.save(formativa)
    }

    private fun abrirFormativaOutroCurso(): Formativa {
        usuariosIt.criarUsuario(EMAIL_ALUNO_OUTRO, "GRR20248899", listOf("dashboard.view_own"))
        val agora = OffsetDateTime.now()
        val alunoOutro = criarAluno(outroCursoId, agora, "GRR20248899", EMAIL_ALUNO_OUTRO)
        val formativa = Formativa.viaPresenca(
            Uuids.v7(),
            alunoOutro,
            Uuids.v7(),
            "Pool CAAF fora",
            4,
            agora,
        )
        formativa.confirmar(agora)
        return formativaRepository.save(formativa)
    }

    private fun garantirCurso(codigo: String, nome: String, sigla: String, agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(Curso(Uuids.v7(), nome, sigla, codigo, null, 120, true, agora, agora)).id
    }

    private fun criarAluno(
        idCurso: UUID,
        agora: OffsetDateTime,
        grr: String = GRR_ALUNO,
        email: String = EMAIL_ALUNO,
    ): UUID {
        val existente = alunoRepository.findByGrr(grr)
        if (existente.isPresent) {
            return existente.get().id
        }
        return alunoRepository.save(
            Aluno(
                Uuids.v7(),
                "Aluno CAAF IT",
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

    private fun contarOutbox(): Int =
        jdbcTemplate.queryForObject("select count(*) from outbox_event", Int::class.java) ?: 0

    companion object {
        private const val CODIGO = "TCAF"
        private const val CODIGO_OUTRO = "ECAF"
        private const val EMAIL_ALUNO = "aluno.caaf.it@ufpr.br"
        private const val EMAIL_ALUNO_OUTRO = "aluno.caaf.outro@ufpr.br"
        private const val EMAIL_CAAF = "caaf.pool.it@ufpr.br"
        private const val EMAIL_COLEGA = "caaf.colegait@ufpr.br"
        private const val EMAIL_ALHEIO = "caaf.alheio.it@ufpr.br"
        private const val EMAIL_SEMCAP = "sem.caaf.it@ufpr.br"
        private const val GRR_ALUNO = "GRR20248801"
        private const val GRR_CAAF = "GRR20248804"
        private const val GRR_COLEGA = "GRR20248805"
        private const val GRR_ALHEIO = "GRR20248806"
        private const val GRR_SEMCAP = "GRR20248807"
    }
}
