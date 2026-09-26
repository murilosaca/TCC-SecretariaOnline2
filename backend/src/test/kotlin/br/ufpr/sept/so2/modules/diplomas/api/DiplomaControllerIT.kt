package br.ufpr.sept.so2.modules.diplomas.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.PeriodoLetivoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo
import br.ufpr.sept.so2.modules.arquivos.application.ports.ObjectStoragePort
import br.ufpr.sept.so2.modules.arquivos.domain.StorageKey
import br.ufpr.sept.so2.modules.diplomas.application.ports.DiplomaRepository
import br.ufpr.sept.so2.modules.formativas.application.ports.FormativaRepository
import br.ufpr.sept.so2.modules.formativas.domain.Formativa
import br.ufpr.sept.so2.modules.formativas.domain.FormativaEstado
import br.ufpr.sept.so2.modules.formativas.domain.FormativaOrigem
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.tcc.application.ports.TccRepository
import br.ufpr.sept.so2.modules.tcc.domain.MembroBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.PapelBancaTcc
import br.ufpr.sept.so2.modules.tcc.domain.Tcc
import br.ufpr.sept.so2.modules.tcc.domain.TccEstado
import br.ufpr.sept.so2.modules.tcc.domain.TccSituacao
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DiplomaControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var cursoRepository: CursoRepository

    @Autowired
    private lateinit var periodoLetivoRepository: PeriodoLetivoRepository

    @Autowired
    private lateinit var alunoRepository: AlunoRepository

    @Autowired
    private lateinit var tccRepository: TccRepository

    @Autowired
    private lateinit var formativaRepository: FormativaRepository

    @Autowired
    private lateinit var diplomaRepository: DiplomaRepository

    @Autowired
    private lateinit var objectStoragePort: ObjectStoragePort

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    private lateinit var cursoId: UUID
    private lateinit var periodoId: UUID
    private lateinit var alunoElegivelId: UUID
    private lateinit var alunoInelegivelId: UUID
    private lateinit var orientadorId: UUID

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.parse("2026-06-01T12:00:00Z")
        cursoId = garantirCurso(agora)
        periodoId = garantirPeriodo(agora)
        usuariosIt.criarUsuario(EMAIL_SECRETARIA, GRR_SECRETARIA, listOf("diploma.register", "course.manage"))
        usuariosIt.criarUsuario(EMAIL_SEM_CAP, GRR_SEM_CAP, listOf("course.manage"))
        orientadorId = usuariosIt.criarUsuario(EMAIL_ORIENTADOR, GRR_ORIENTADOR, listOf("tcc.review")).id
        usuariosIt.criarUsuario(EMAIL_ALUNO_OK, GRR_ALUNO_OK, listOf(
            "dashboard.view_own",
            "request.open",
            "tcc.view_own",
            "formative.view_own",
        ))
        usuariosIt.criarUsuario(EMAIL_ALUNO_NOK, GRR_ALUNO_NOK, listOf(
            "dashboard.view_own",
            "request.open",
            "tcc.view_own",
        ))
        alunoElegivelId = garantirAluno("Elegível Colação", GRR_ALUNO_OK, EMAIL_ALUNO_OK, cursoId, agora)
        alunoInelegivelId = garantirAluno("Inelegível Colação", GRR_ALUNO_NOK, EMAIL_ALUNO_NOK, cursoId, agora)
        garantirTccAprovado(alunoElegivelId, cursoId, orientadorId, agora)
        garantirHoras(alunoElegivelId, agora, 120)
    }

    @Test
    fun semCapabilityRecebe403() {
        val token = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(
            get("/diplomas/elegiveis")
                .param("cursoId", cursoId.toString())
                .param("periodoId", periodoId.toString())
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun elegiveisMarcaInelegivelEConfirmaComLink() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        mockMvc.perform(
            get("/diplomas/elegiveis")
                .param("cursoId", cursoId.toString())
                .param("periodoId", periodoId.toString())
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(2)))
            .andExpect(jsonPath("$._links.confirm").value("/diplomas"))
            .andExpect(jsonPath("$.content[?(@.alunoId=='$alunoElegivelId')].elegivel").value(true))
            .andExpect(jsonPath("$.content[?(@.alunoId=='$alunoInelegivelId')].elegivel").value(false))
            .andExpect(
                jsonPath("$.content[?(@.alunoId=='$alunoInelegivelId')].bloqueio.razao")
                    .value("TCC não aprovado"),
            )
    }

    @Test
    fun colacaoEntregaPdfEPainelEgresso() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        val auditAntes = contarAudit("egressos.graduated")

        mockMvc.perform(
            post("/diplomas")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "cursoId":"$cursoId",
                      "periodoId":"$periodoId",
                      "alunoIds":["$alunoElegivelId"],
                      "dataColacao":"2026-11-15T15:00:00Z",
                      "livro":"12",
                      "folha":"3",
                      "turma":"2026/2"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].situacao").value("PENDENTE"))
            .andExpect(jsonPath("$[0]._links['confirm-delivery']").exists())
            .andExpect(jsonPath("$[0]._links['upload-pdf']").exists())

        val diplomaId = diplomaRepository.findByAluno(alunoElegivelId)!!.id

        assertEquals(AlunoSituacao.EGRESSO, alunoRepository.findById(alunoElegivelId).get().situacao)
        val usuario = usuarioRepository.findByEmail(EMAIL_ALUNO_OK).get()
        assertEquals(listOf("alumni.view_own"), usuario.authorities)
        assertTrue(contarAudit("egressos.graduated") > auditAntes)
        assertTrue(contarOutbox("egressos.graduated") >= 1)

        val pdf = MockMultipartFile(
            "file",
            "diploma.pdf",
            "application/pdf",
            "%PDF-1.4 diploma-oficial\n".toByteArray(),
        )
        mockMvc.perform(
            multipart("/diplomas/$diplomaId/pdf")
                .file(pdf)
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.temPdf").value(true))

        mockMvc.perform(
            patch("/diplomas/$diplomaId/confirm-delivery")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"metodo":"PRESENCIAL","dataEntrega":"2026-11-20T14:00:00Z"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.situacao").value("ENTREGUE"))
            .andExpect(jsonPath("$.metodoEntrega").value("PRESENCIAL"))
            .andExpect(jsonPath("$._links['confirm-delivery']").doesNotExist())

        val diploma = diplomaRepository.findById(diplomaId)!!
        assertTrue(objectStoragePort.exists(StorageKey.diploma(diplomaId).value))

        val egressoToken = usuariosIt.login(EMAIL_ALUNO_OK)
        mockMvc.perform(get("/egressos/me").header("Authorization", "Bearer $egressoToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.concluidoEm").exists())
            .andExpect(jsonPath("$.kpis.situacaoDiploma").value("EMITIDO"))
            .andExpect(jsonPath("$.diploma.numero").value(diploma.numero))
            .andExpect(jsonPath("$.diploma._links.download").value("/egressos/me/diploma"))
            .andExpect(jsonPath("$.colacao.data").exists())
            .andExpect(jsonPath("$.colacao.turma").value("2026/2"))

        mockMvc.perform(get("/egressos/me/diploma").header("Authorization", "Bearer $egressoToken"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.downloadUrl").isString)
            .andExpect(jsonPath("$.expiresInSeconds").value(900))
    }

    @Test
    fun menuTemDiplomasComCapability() {
        val token = usuariosIt.login(EMAIL_SECRETARIA)
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.diplomas").value("/secretaria/diplomas"))
    }

    private fun garantirCurso(agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(CODIGO)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(
            Curso(Uuids.v7(), "TADS Diploma IT", "DIP", CODIGO, null, 120, true, agora, agora),
        ).id
    }

    private fun garantirPeriodo(agora: OffsetDateTime): UUID {
        val todos = periodoLetivoRepository.findAll()
        val existente = todos.find { it.ano == 2026 && it.semestre == 2 }
        if (existente != null) {
            return existente.id
        }
        return periodoLetivoRepository.save(
            PeriodoLetivo(
                Uuids.v7(),
                2026,
                2,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 12, 20),
                true,
                agora,
                agora,
            ),
        ).id
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
            val aluno = existente.get()
            if (aluno.situacao == AlunoSituacao.EGRESSO) {
                aluno.atualizar(null, null, null, null, idCurso, AlunoSituacao.MATRICULADO, true)
                alunoRepository.save(aluno)
            }
            diplomaRepository.findByAluno(aluno.id)?.let {
                jdbcTemplate.update("delete from diploma where id = ?", it.id)
            }
            usuarioRepository.findByEmail(email).ifPresent { usuario ->
                usuario.substituirAuthorities(
                    listOf("dashboard.view_own", "request.open", "tcc.view_own", "formative.view_own"),
                    OffsetDateTime.now(),
                )
                usuarioRepository.save(usuario)
            }
            return aluno.id
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

    private fun garantirTccAprovado(alunoId: UUID, cursoId: UUID, orientadorId: UUID, agora: OffsetDateTime) {
        val pagina = tccRepository.findByAluno(alunoId, TccEstado.APROVADO, org.springframework.data.domain.PageRequest.of(0, 1))
        if (pagina.hasContent()) {
            return
        }
        tccRepository.save(
            Tcc(
                Uuids.v7(),
                alunoId,
                cursoId,
                "TCC Colação IT",
                TccSituacao.CONCLUIDO,
                TccEstado.APROVADO,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 9, 1),
                null,
                null,
                null,
                null,
                null,
                agora,
                agora,
                listOf(MembroBancaTcc(Uuids.v7(), orientadorId, PapelBancaTcc.ORIENTADOR)),
            ),
        )
    }

    private fun garantirHoras(alunoId: UUID, agora: OffsetDateTime, horas: Int) {
        val eventoId = UUID.fromString("01999999-0000-7000-8000-00000000d016")
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
                "Horas colação",
                horas,
                FormativaEstado.APROVADA,
                agora,
                agora,
            ),
        )
    }

    private fun contarAudit(tipo: String): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from audit_log where tipo = ?",
            Int::class.java,
            tipo,
        ) ?: 0

    private fun contarOutbox(tipo: String): Int =
        jdbcTemplate.queryForObject(
            "select count(*) from outbox_event where tipo = ?",
            Int::class.java,
            tipo,
        ) ?: 0

    companion object {
        private const val EMAIL_SECRETARIA = "it.diploma.secretaria@ufpr.br"
        private const val EMAIL_SEM_CAP = "it.diploma.semcap@ufpr.br"
        private const val EMAIL_ORIENTADOR = "it.diploma.orientador@ufpr.br"
        private const val EMAIL_ALUNO_OK = "it.diploma.aluno.ok@ufpr.br"
        private const val EMAIL_ALUNO_NOK = "it.diploma.aluno.nok@ufpr.br"
        private const val GRR_SECRETARIA = "GRR20261601"
        private const val GRR_SEM_CAP = "GRR20261602"
        private const val GRR_ORIENTADOR = "GRR20261603"
        private const val GRR_ALUNO_OK = "GRR20261604"
        private const val GRR_ALUNO_NOK = "GRR20261605"
        private const val CODIGO = "TADS-DIPLOMA-IT"
    }
}
