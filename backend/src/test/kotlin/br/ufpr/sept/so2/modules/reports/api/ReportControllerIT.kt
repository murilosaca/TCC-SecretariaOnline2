package br.ufpr.sept.so2.modules.reports.api

import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.infrastructure.Uuids
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
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerIT {

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
    private lateinit var periodoLetivoRepository: PeriodoLetivoRepository

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoTadsId: UUID
    private lateinit var cursoEcId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.parse("2026-06-01T12:00:00Z")
        val coord = usuariosIt.criarUsuario(
            EMAIL_COORD,
            GRR_COORD,
            listOf("report.view_coordinator", "course.config", "request.deliberate", "tcc.review"),
        )
        val outroCoord = usuariosIt.criarUsuario(
            EMAIL_OUTRO,
            GRR_OUTRO,
            listOf("report.view_coordinator", "course.config"),
        )
        val secretaria = usuariosIt.criarUsuario(
            EMAIL_SECRETARIA,
            GRR_SECRETARIA,
            listOf("course.manage", "report.view_secretary", "request.view_curso"),
        )
        usuariosIt.criarUsuario(EMAIL_SEM_CAP, GRR_SEM_CAP, listOf("course.config"))
        usuariosIt.criarUsuario(
            EMAIL_SEC_SEM_VINCULO,
            GRR_SEC_SEM_VINCULO,
            listOf("course.manage", "report.view_secretary"),
        )
        cursoTadsId = garantirCurso(CODIGO_TADS, "TADS F62", "T62", coord.id, agora)
        cursoEcId = garantirCurso(CODIGO_EC, "EC F62", "E62", outroCoord.id, agora)
        cursoSecretarioRepository.adicionarSeAusente(cursoTadsId, secretaria.id)
        garantirPeriodo(2026, 2, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 12, 15), agora)
        garantirPeriodo(2026, 1, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), agora)
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/reports/coordinator"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityRecebe403() {
        val token = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(get("/reports/coordinator").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.relatorios").doesNotExist())
    }

    @Test
    fun secretariaSemCapRecebe403NoCoordinator() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        mockMvc.perform(get("/reports/coordinator").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun coordenadorPuroSemCapRecebe403NoSecretaryESemMenu() {
        val token = usuariosIt.login(EMAIL_COORD)
        mockMvc.perform(get("/reports/secretary").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.estatisticas").doesNotExist())
    }

    @Test
    fun secretariaLeDatasetsDoEscopoEGanhaMenu() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.estatisticas").value("/secretaria/estatisticas"))

        mockMvc.perform(
            get("/reports/secretary")
                .param("periodo", "2026-2")
                .param("curso", "T62")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.filtros.curso").value("T62"))
            .andExpect(jsonPath("$.filtros.cursoId").value(cursoTadsId.toString()))
            .andExpect(jsonPath("$.filtros.periodo").value("2026-2"))
            .andExpect(jsonPath("$.filtros.cursos").isArray)
            .andExpect(jsonPath("$.solicitacoesPorTipo").isArray)
            .andExpect(jsonPath("$.solicitacoesPorEstado").isArray)
            .andExpect(jsonPath("$.presencas").isArray)
            .andExpect(jsonPath("$.horasFormativas").isArray)
            .andExpect(jsonPath("$.itensSolicitacao").isArray)
            .andExpect(jsonPath("$._links.self").value("/reports/secretary?periodo=2026-2&curso=T62"))
            .andExpect(jsonPath("$._links.estatisticas").value("/secretaria/estatisticas"))
            .andExpect(jsonPath("$._links.deliberar").value("/solicitacoes?to=me"))
    }

    @Test
    fun secretariaSemCursoAgregaTodosVinculados() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        mockMvc.perform(get("/reports/secretary").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.filtros.curso").doesNotExist())
            .andExpect(jsonPath("$.filtros.cursoNome").value("Cursos vinculados"))
            .andExpect(jsonPath("$.filtros.cursos[0].sigla").value("T62"))
            .andExpect(jsonPath("$._links.self").exists())
    }

    @Test
    fun secretariaCursoForaDoEscopoDevolve403() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        mockMvc.perform(
            get("/reports/secretary")
                .param("curso", "E62")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Curso fora do escopo da sua secretaria."))
            .andExpect(jsonPath("$.solicitacoesPorTipo").doesNotExist())
    }

    @Test
    fun secretariaSemVinculoDevolve403() {
        val token = usuariosIt.login(EMAIL_SEC_SEM_VINCULO)
        mockMvc.perform(get("/reports/secretary").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Nenhum curso vinculado à sua secretaria foi encontrado."))
    }

    @Test
    fun coordenadorLeKpisDoProprioCursoEGanhaMenu() {
        val token = usuariosIt.login(EMAIL_COORD)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.relatorios").value("/coordenacao/relatorios"))

        mockMvc.perform(
            get("/reports/coordinator")
                .param("periodo", "2026-2")
                .param("curso", "T62")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.filtros.curso").value("T62"))
            .andExpect(jsonPath("$.filtros.cursoId").value(cursoTadsId.toString()))
            .andExpect(jsonPath("$.filtros.periodo").value("2026-2"))
            .andExpect(jsonPath("$.kpis.thresholdIndeferimento").value(0.2))
            .andExpect(jsonPath("$.kpis.tempoMedioDias").exists())
            .andExpect(jsonPath("$.kpis.taxaIndeferimento").exists())
            .andExpect(jsonPath("$.kpis.horasValidadas").exists())
            .andExpect(jsonPath("$.kpis.taxaPresenca").exists())
            .andExpect(jsonPath("$.series.evasao").isArray)
            .andExpect(jsonPath("$.series.formativas").isArray)
            .andExpect(jsonPath("$.series.aprovacaoFormativas").isArray)
            .andExpect(jsonPath("$.pendencias").isArray)
            .andExpect(jsonPath("$.cargaPorDeliberador").isArray)
            .andExpect(jsonPath("$._links.self").value("/reports/coordinator?periodo=2026-2&curso=T62"))
            .andExpect(jsonPath("$._links['configurar-curso']").value("/coordenacao/cursos/$cursoTadsId/configurar"))
            .andExpect(jsonPath("$._links.deliberar").value("/solicitacoes?to=me"))
            .andExpect(jsonPath("$._links['tipos-solicitacao']").doesNotExist())
    }

    @Test
    fun cursoDeOutroCoordenadorDevolve403() {
        val token = usuariosIt.login(EMAIL_COORD)
        mockMvc.perform(
            get("/reports/coordinator")
                .param("curso", "E62")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value("Você não é coordenador deste curso."))
            .andExpect(jsonPath("$.kpis").doesNotExist())
    }

    @Test
    fun periodoInvalidoDevolve422() {
        val token = usuariosIt.login(EMAIL_COORD)
        mockMvc.perform(
            get("/reports/coordinator")
                .param("periodo", "2026/2")
                .param("curso", "T62")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isUnprocessableEntity)
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

    private fun garantirPeriodo(
        ano: Int,
        semestre: Int,
        inicio: LocalDate,
        fim: LocalDate,
        agora: OffsetDateTime,
    ) {
        val existente = periodoLetivoRepository.findAll().firstOrNull { it.ano == ano && it.semestre == semestre }
        if (existente != null) {
            return
        }
        periodoLetivoRepository.save(
            PeriodoLetivo(Uuids.v7(), ano, semestre, inicio, fim, true, agora, agora),
        )
    }

    companion object {
        private const val EMAIL_COORD = "coord.relatorio@ufpr.br"
        private const val GRR_COORD = "GRR62000001"
        private const val EMAIL_OUTRO = "coord.outro.relatorio@ufpr.br"
        private const val GRR_OUTRO = "GRR62000002"
        private const val EMAIL_SECRETARIA = "secretaria.relatorio@ufpr.br"
        private const val GRR_SECRETARIA = "GRR62000003"
        private const val EMAIL_SEM_CAP = "prof.sem.relatorio@ufpr.br"
        private const val GRR_SEM_CAP = "GRR62000004"
        private const val EMAIL_SEC_SEM_VINCULO = "secretaria.sem.vinculo@ufpr.br"
        private const val GRR_SEC_SEM_VINCULO = "GRR62000005"
        private const val CODIGO_TADS = "TADS-F62-REP"
        private const val CODIGO_EC = "EC-F62-REP"
    }
}
