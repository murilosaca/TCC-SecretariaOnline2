package br.ufpr.sept.so2.modules.bff.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoSecretarioRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.bff.application.SecretariaDashboardRegras
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao
import br.ufpr.sept.so2.modules.solicitacoes.domain.SolicitacaoEvento
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
import br.ufpr.sept.so2.shared.ItJson
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.hasSize
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
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecretaryDashboardControllerIT {

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
    private lateinit var tipoSolicitacaoRepository: TipoSolicitacaoRepository

    @Autowired
    private lateinit var solicitacaoRepository: SolicitacaoRepository

    @Autowired
    private lateinit var eventoRepository: EventoRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var anfitriaoId: UUID

    @BeforeEach
    fun seed() {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val agora = OffsetDateTime.now()
            if (tipoSolicitacaoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty) {
                tipoSolicitacaoRepository.save(DeclaracaoSimplesSeed.tipo(agora))
            }
            val sec = usuariosIt.criarUsuario(EMAIL_SEC, GRR_SEC, CAPS_SEC)
            usuariosIt.criarUsuario(EMAIL_SEM_CURSO, GRR_SEM_CURSO, listOf("dashboard.view_secretary"))
            usuariosIt.criarUsuario(EMAIL_SEM_CAP, GRR_SEM_CAP, listOf("course.manage"))
            val alunoA = usuariosIt.criarUsuario(EMAIL_ALUNO_A, GRR_ALUNO_A, CAPS_ALUNO)
            val alunoB = usuariosIt.criarUsuario(EMAIL_ALUNO_B, GRR_ALUNO_B, CAPS_ALUNO)
            val prof = usuariosIt.criarUsuario(EMAIL_PROF, GRR_PROF, CAPS_PROF)
            anfitriaoId = prof.id
            val cursoA = curso("Curso Dash A", "DSA", "DASH-SEC-A", agora)
            val cursoB = curso("Curso Dash B", "DSB", "DASH-SEC-B", agora)
            cursoSecretarioRepository.replaceAll(cursoA.id, listOf(sec.id))
            cursoSecretarioRepository.replaceAll(cursoB.id, emptyList())
            alunoAcademico(GRR_ALUNO_A, EMAIL_ALUNO_A, cursoA.id, agora)
            alunoAcademico(GRR_ALUNO_B, EMAIL_ALUNO_B, cursoB.id, agora)
            check(alunoA.id != alunoB.id)
        }
    }

    @Test
    fun anonimoRecebe401() {
        mockMvc.perform(get("/bff/dashboard/secretary"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun semCapabilityRecebe403ENaoConsultaOPayload() {
        val aluno = usuariosIt.login(EMAIL_ALUNO_A)
        mockMvc.perform(get("/bff/dashboard/secretary").header("Authorization", "Bearer $aluno"))
            .andExpect(status().isForbidden)
        val prof = usuariosIt.login(EMAIL_PROF)
        mockMvc.perform(get("/bff/dashboard/secretary").header("Authorization", "Bearer $prof"))
            .andExpect(status().isForbidden)
        val semCap = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(get("/bff/dashboard/secretary").header("Authorization", "Bearer $semCap"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun semCursoVinculadoRecebe403() {
        val token = usuariosIt.login(EMAIL_SEM_CURSO)
        mockMvc.perform(get("/bff/dashboard/secretary").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.detail").value(SecretariaDashboardRegras.MSG_SEM_CURSO))
    }

    @Test
    fun painelEmAuthMeApontaOBffENaoMisturaPerfis() {
        val sec = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $sec"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.painel").value("/bff/dashboard/secretary"))
            .andExpect(jsonPath("$._links.inicio").value("/inicio"))

        val aluno = usuariosIt.login(EMAIL_ALUNO_A)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $aluno"))
            .andExpect(jsonPath("$._links.painel").value("/bff/dashboard/aluno"))

        val prof = usuariosIt.login(EMAIL_PROF)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $prof"))
            .andExpect(jsonPath("$._links.painel").value("/bff/dashboard/professor"))
    }

    @Test
    fun secretariaVeSoOCursoVinculado() {
        val agora = OffsetDateTime.now()
        val marcoHoje = agora.toLocalDate().atStartOfDay().atOffset(agora.offset).plusMinutes(1)
        val atrasada = abrir(EMAIL_ALUNO_A)
        val aviso = abrir(EMAIL_ALUNO_A)
        val concluidaHoje = abrir(EMAIL_ALUNO_A)
        val concluidaOntem = abrir(EMAIL_ALUNO_A)
        val fora = abrir(EMAIL_ALUNO_B)
        val foraConcluida = abrir(EMAIL_ALUNO_B)
        forcarPrazo(atrasada.id, agora.minusDays(2))
        forcarPrazo(aviso.id, agora.plusHours(12))
        marcarConcluida(concluidaHoje.id, marcoHoje)
        marcarConcluida(concluidaOntem.id, marcoHoje.minusDays(1))
        marcarConcluida(foraConcluida.id, marcoHoje)
        evento("Agenda secretaria IT hoje", marcoHoje, marcoHoje.plusHours(1))
        evento("Agenda secretaria IT ontem", marcoHoje.minusDays(1), marcoHoje.minusDays(1).plusHours(1))

        val token = usuariosIt.login(EMAIL_SEC)
        mockMvc.perform(get("/bff/dashboard/secretary").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.kpis.abertas").value(2))
            .andExpect(jsonPath("$.kpis.atrasadas").value(1))
            .andExpect(jsonPath("$.kpis.concluidasHoje").value(1))
            .andExpect(jsonPath("$.kpis.eventosDia").isNumber)
            .andExpect(jsonPath("$.alertasSla").value(1))
            .andExpect(jsonPath("$.filaPriorizada", hasSize<Any>(2)))
            .andExpect(jsonPath("$.filaPriorizada[0].protocolo").value(atrasada.protocolo))
            .andExpect(jsonPath("$.filaPriorizada[0].slaStatus").value("danger"))
            .andExpect(jsonPath("$.filaPriorizada[1].protocolo").value(aviso.protocolo))
            .andExpect(jsonPath("$.filaPriorizada[1].slaStatus").value("warning"))
            .andExpect(jsonPath("$.filaPriorizada[?(@.protocolo=='${fora.protocolo}')]").isEmpty())
            .andExpect(jsonPath("$.agendaDia[?(@.titulo=='Agenda secretaria IT hoje')]").isNotEmpty())
            .andExpect(jsonPath("$.agendaDia[?(@.titulo=='Agenda secretaria IT ontem')]").isEmpty())
            .andExpect(jsonPath("$._links.self").value("/bff/dashboard/secretary"))
            .andExpect(jsonPath("$._links.cursos").value("/secretaria/cursos"))
            .andExpect(jsonPath("$._links.alunos").value("/secretaria/alunos"))
            .andExpect(jsonPath("$._links['fila-solicitacoes']").value("/solicitacoes"))
            .andExpect(jsonPath("$._links.importacoes").doesNotExist())
            .andExpect(jsonPath("$.alertaPeriodoAusente").isBoolean())
    }

    private fun abrir(email: String): Aberta {
        val token = usuariosIt.login(email)
        val created = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PAYLOAD),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val body = created.response.contentAsString
        return Aberta(UUID.fromString(ItJson.text(body, "id")), ItJson.text(body, "protocolo"))
    }

    private fun forcarPrazo(id: UUID, prazo: OffsetDateTime) {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val atual = solicitacaoRepository.findById(id).orElseThrow()
            solicitacaoRepository.save(copia(atual, atual.estado, prazo, atual.eventos, atual.updatedAt ?: OffsetDateTime.now()))
        }
    }

    private fun marcarConcluida(id: UUID, quando: OffsetDateTime) {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val atual = solicitacaoRepository.findById(id).orElseThrow()
            val evento = SolicitacaoEvento(
                Uuids.v7(),
                Solicitacao.EVENTO_TRANSICAO,
                atual.estado,
                SecretariaDashboardRegras.ESTADO_CONCLUIDA,
                null,
                null,
                null,
                quando,
            )
            solicitacaoRepository.save(
                copia(atual, SecretariaDashboardRegras.ESTADO_CONCLUIDA, atual.prazoEm, atual.eventos + evento, quando),
            )
        }
    }

    private fun copia(
        atual: Solicitacao,
        estado: String,
        prazo: OffsetDateTime?,
        eventos: List<SolicitacaoEvento>,
        updatedAt: OffsetDateTime,
    ): Solicitacao =
        Solicitacao(
            atual.id,
            atual.tipoId,
            atual.tipoCodigo,
            atual.tipoNome,
            atual.tipoVersao,
            atual.solicitanteId,
            atual.protocolo,
            estado,
            atual.payloadJson,
            atual.formSchemaSnapshot,
            atual.workflowSnapshot,
            prazo,
            atual.hashSha256,
            atual.deliberadorId,
            eventos,
            atual.createdAt,
            updatedAt,
        )

    private fun evento(titulo: String, inicio: OffsetDateTime, fim: OffsetDateTime) {
        if (eventoRepository.findByTitulo(titulo).isPresent) {
            return
        }
        eventoRepository.save(
            Evento.criar(Uuids.v7(), anfitriaoId, titulo, inicio, fim, 2, AttendanceMode.SECRET_SINGLE, OffsetDateTime.now()),
        )
    }

    private fun curso(nome: String, sigla: String, codigo: String, agora: OffsetDateTime): Curso {
        val existente = cursoRepository.findByCodigo(codigo)
        if (existente.isPresent) {
            return existente.get()
        }
        return cursoRepository.save(Curso(Uuids.v7(), nome, sigla, codigo, null, 120, true, agora, agora))
    }

    private fun alunoAcademico(grr: String, email: String, idCurso: UUID, agora: OffsetDateTime) {
        if (alunoRepository.findByGrr(grr).isPresent) {
            return
        }
        alunoRepository.save(
            Aluno(
                Uuids.v7(),
                "Aluno $grr",
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
        )
    }

    private data class Aberta(val id: UUID, val protocolo: String)

    companion object {
        private const val EMAIL_SEC = "it.dash.sec@ufpr.br"
        private const val EMAIL_SEM_CURSO = "it.dash.semcurso@ufpr.br"
        private const val EMAIL_SEM_CAP = "it.dash.semcap@ufpr.br"
        private const val EMAIL_ALUNO_A = "it.dash.aluno.a@ufpr.br"
        private const val EMAIL_ALUNO_B = "it.dash.aluno.b@ufpr.br"
        private const val EMAIL_PROF = "it.dash.prof@ufpr.br"
        private const val GRR_SEC = "GRR20242101"
        private const val GRR_SEM_CURSO = "GRR20242102"
        private const val GRR_SEM_CAP = "GRR20242103"
        private const val GRR_ALUNO_A = "GRR20242104"
        private const val GRR_ALUNO_B = "GRR20242105"
        private const val GRR_PROF = "GRR20242106"
        private val CAPS_SEC = listOf(
            "dashboard.view_secretary",
            "course.manage",
            "user.manage_students",
            "request.view_curso",
        )
        private val CAPS_ALUNO = listOf(
            "dashboard.view_own",
            "request.view_own",
            "request.open",
            "attendance.view_open",
        )
        private val CAPS_PROF = listOf("dashboard.view_self_professor", "request.deliberate", "event.manage")
        private const val PAYLOAD = """
            {"tipoCodigo":"DECLARACAO_SIMPLES","payload":{"finalidade":"Comprovação de vínculo"}}
            """
    }
}
