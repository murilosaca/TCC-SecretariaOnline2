package br.ufpr.sept.so2.modules.formativas.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado
import br.ufpr.sept.so2.shared.ItUsuarioFixture
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
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
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RevisaoFormativaIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var eventoRepository: EventoRepository

    @Autowired
    private lateinit var cursoRepository: CursoRepository

    @Autowired
    private lateinit var alunoRepository: AlunoRepository

    private val usuariosIt
        get() = ItUsuarioFixture(usuarioRepository, passwordHasher, mockMvc)

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        val cursoId = garantirCurso(agora)
        usuariosIt.criarUsuario(EMAIL_ALUNO, "GRR20248901", AUTHORITIES_ALUNO)
        criarAlunoSeAusente("Aluno Revisão IT", "GRR20248901", EMAIL_ALUNO, cursoId, agora)
        usuariosIt.criarUsuario(EMAIL_OUTRO, "GRR20248902", AUTHORITIES_ALUNO)
        criarAlunoSeAusente("Outro Aluno IT", "GRR20248902", EMAIL_OUTRO, cursoId, agora)
        usuariosIt.criarUsuario(
            EMAIL_PROFESSOR,
            "GRR20248903",
            listOf("dashboard.view_own", "event.manage", "event.host", "request.deliberate"),
        )
        usuariosIt.criarUsuario(EMAIL_CAAF, "GRR20248904", listOf("dashboard.view_own", "formative.review"))
    }

    @Test
    fun alunoConfirmaCaafAprovaBffSomaHorasEProfessorSemReviewToma403() {
        val aberto = salvarEvento()
        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)

        mockMvc.perform(
            post("/events/${aberto.id}/attendance/confirm")
                .header("Authorization", "Bearer $tokenAluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(confirmBody()),
        )
            .andExpect(status().isOk)

        val listaAluno = mockMvc.perform(
            get("/formativas")
                .param("audience", "me")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].estado").value("PENDENTE_CONFIRMACAO"))
            .andReturn()
            .response
            .contentAsString
        val formativaId = extract(listaAluno, "\"id\":\"", "\"")

        mockMvc.perform(
            post("/formativas/$formativaId/confirmar")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("AGUARDANDO_CAAF"))
            .andExpect(jsonPath("$._links.aprovar").doesNotExist())

        mockMvc.perform(
            get("/formativas")
                .param("canReview", "true")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isForbidden)

        val tokenProfessor = usuariosIt.login(EMAIL_PROFESSOR)
        mockMvc.perform(
            get("/formativas")
                .param("canReview", "true")
                .header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            post("/formativas/$formativaId/aprovar")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"parecer":"Professor sem formative.review não aprova."}"""),
        )
            .andExpect(status().isForbidden)

        val tokenOutro = usuariosIt.login(EMAIL_OUTRO)
        mockMvc.perform(
            get("/formativas/$formativaId")
                .header("Authorization", "Bearer $tokenOutro"),
        )
            .andExpect(status().isNotFound)

        val tokenCaaf = usuariosIt.login(EMAIL_CAAF)
        mockMvc.perform(
            get("/formativas")
                .param("canReview", "true")
                .header("Authorization", "Bearer $tokenCaaf"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", hasItem(formativaId)))
            .andExpect(jsonPath("$.content[*]._links.aprovar", hasItem("/formativas/$formativaId/aprovar")))
            .andExpect(jsonPath("$.content[*]._links.indeferir", hasItem("/formativas/$formativaId/indeferir")))
            .andExpect(jsonPath("$.content[*]._links.revisar", hasItem("/formativas/$formativaId")))

        mockMvc.perform(
            post("/formativas/$formativaId/indeferir")
                .header("Authorization", "Bearer $tokenCaaf")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"parecer":"curto"}"""),
        )
            .andExpect(status().isUnprocessableEntity)

        mockMvc.perform(
            post("/formativas/$formativaId/aprovar")
                .header("Authorization", "Bearer $tokenCaaf")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"parecer":"Presença validada; horas da oficina mantidas."}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("APROVADA"))
            .andExpect(jsonPath("$.cargaHoraria").value(aberto.cargaHoraria))
            .andExpect(jsonPath("$._links.aprovar").doesNotExist())
            .andExpect(jsonPath("$._links.indeferir").doesNotExist())

        mockMvc.perform(
            get("/formativas")
                .param("canReview", "true")
                .header("Authorization", "Bearer $tokenCaaf"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", not(hasItem(formativaId))))

        mockMvc.perform(
            get("/formativas/$formativaId")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("APROVADA"))
            .andExpect(jsonPath("$.parecer").value("Presença validada; horas da oficina mantidas."))

        mockMvc.perform(
            get("/bff/dashboard/aluno")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.kpis.horasFormativas.validadas", greaterThan(0)))
            .andExpect(jsonPath("$.kpis.horasFormativas.requeridas").value(120))
    }

    private fun garantirCurso(agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(CODIGO)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(
            Curso(Uuids.v7(), "TADS IT Revisão", "TADSR", CODIGO, null, 120, true, agora, agora),
        ).id
    }

    private fun criarAlunoSeAusente(
        nome: String,
        grr: String,
        email: String,
        idCurso: UUID,
        agora: OffsetDateTime,
    ) {
        if (alunoRepository.findByGrr(grr).isPresent) {
            return
        }
        alunoRepository.save(
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
        )
    }

    private fun salvarEvento(): Evento {
        val agora = OffsetDateTime.now()
        val janelaInicio = agora.minusMinutes(1)
        val janelaFim = agora.plusMinutes(30)
        return eventoRepository.save(
            Evento(
                Uuids.v7(),
                Uuids.v7(),
                "IT revisão ${Uuids.v7()}",
                janelaInicio.minusMinutes(10),
                janelaFim.plusHours(1),
                4,
                AttendanceMode.SECRET_SINGLE,
                EventoEstado.EM_ANDAMENTO,
                passwordHasher.hash(PIN),
                janelaInicio,
                janelaFim,
                null,
                null,
                agora,
                agora,
            ),
        )
    }

    companion object {
        private const val PIN = "123456"
        private const val EMAIL_ALUNO = "it.revisao.aluno@ufpr.br"
        private const val EMAIL_OUTRO = "it.revisao.outro@ufpr.br"
        private const val EMAIL_PROFESSOR = "it.revisao.prof@ufpr.br"
        private const val EMAIL_CAAF = "it.revisao.caaf@ufpr.br"
        private const val CODIGO = "TADS-REV-IT"
        private const val DEVICE = "55555555-5555-5555-8555-555555555555"
        private val AUTHORITIES_ALUNO = listOf(
            "dashboard.view_own",
            "attendance.view_open",
            "attendance.check_in",
            "formative.view_own",
            "formative.confirm_own",
        )

        private fun confirmBody(): String =
            "{\"pin\":\"$PIN\",\"deviceUuid\":\"$DEVICE\",\"fase\":\"ENTRADA\"}"

        private fun extract(json: String, startToken: String, endToken: String): String {
            val start = json.indexOf(startToken)
            val from = start + startToken.length
            val end = json.indexOf(endToken, from)
            return json.substring(from, end)
        }
    }
}
