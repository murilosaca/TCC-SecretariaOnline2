package br.ufpr.sept.so2.modules.formativas.api

import br.ufpr.sept.so2.modules.academico.application.ports.AlunoRepository
import br.ufpr.sept.so2.modules.academico.application.ports.CursoRepository
import br.ufpr.sept.so2.modules.academico.domain.Aluno
import br.ufpr.sept.so2.modules.academico.domain.AlunoSituacao
import br.ufpr.sept.so2.modules.academico.domain.Curso
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.modules.presenca.application.ports.EventoRepository
import br.ufpr.sept.so2.modules.presenca.domain.AttendanceMode
import br.ufpr.sept.so2.modules.presenca.domain.Evento
import br.ufpr.sept.so2.modules.presenca.domain.EventoEstado
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
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
class FormativaControllerIT {

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

    @BeforeEach
    fun seed() {
        val agora = OffsetDateTime.now()
        val cursoId = garantirCurso(agora)
        criarUsuarioSeAusente(
            EMAIL_ALUNO,
            "GRR20248801",
            true,
            agora,
            agora,
            AUTHORITIES_ALUNO,
        )
        criarAlunoSeAusente("Aluno Formativa IT", "GRR20248801", EMAIL_ALUNO, cursoId, agora)
        criarUsuarioSeAusente(
            "novo.dev@ufpr.br",
            "GRR20240002",
            false,
            null,
            agora,
            AUTHORITIES_ALUNO,
        )
        criarAlunoSeAusente("Novo Dev", "GRR20240002", "novo.dev@ufpr.br", cursoId, agora)
        criarUsuarioSeAusente(
            EMAIL_PROFESSOR,
            "GRR20248803",
            true,
            agora,
            agora,
            listOf("dashboard.view_own", "event.manage", "event.host"),
        )
    }

    @Test
    fun alunoConfirmaAposPresencaProfessorENovoRecebem403() {
        val aberto = salvarEvento()
        val tokenAluno = login(EMAIL_ALUNO)

        mockMvc.perform(
            post("/events/${aberto.id}/attendance/confirm")
                .header("Authorization", "Bearer $tokenAluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(confirmBody()),
        )
            .andExpect(status().isOk)

        val lista = mockMvc.perform(
            get("/formativas")
                .param("audience", "me")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].titulo").value(aberto.titulo))
            .andExpect(jsonPath("$.content[0].origem").value("PRESENCA_VALIDADA"))
            .andExpect(jsonPath("$.content[0].estado").value("PENDENTE_CONFIRMACAO"))
            .andExpect(jsonPath("$.content[0]._links.confirmar").exists())
            .andExpect(jsonPath("$.content[0]._links.cancelar").exists())
            .andReturn()
            .response
            .contentAsString
        val formativaId = extract(lista, "\"id\":\"", "\"")

        mockMvc.perform(
            post("/formativas/$formativaId/confirmar")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("AGUARDANDO_CAAF"))
            .andExpect(jsonPath("$._links.confirmar").doesNotExist())
            .andExpect(jsonPath("$._links.cancelar").doesNotExist())

        val tokenProfessor = login(EMAIL_PROFESSOR)
        mockMvc.perform(
            get("/formativas")
                .param("audience", "me")
                .header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isForbidden)

        val tokenNovo = login("novo.dev@ufpr.br")
        mockMvc.perform(
            get("/formativas")
                .header("Authorization", "Bearer $tokenNovo"),
        )
            .andExpect(status().isForbidden)
    }

    private fun garantirCurso(agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(CODIGO)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(
            Curso(Uuids.v7(), "TADS IT Formativa", "TADSF", CODIGO, null, 120, true, agora, agora),
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
                "IT formativa ${Uuids.v7()}",
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

    private fun criarUsuarioSeAusente(
        email: String,
        grr: String,
        senhaAlterada: Boolean,
        lgpd: OffsetDateTime?,
        agora: OffsetDateTime,
        authorities: List<String>,
    ) {
        if (usuarioRepository.findByEmail(email).isPresent) {
            return
        }
        usuarioRepository.save(
            Usuario(
                Uuids.v7(),
                Email.of(email),
                null,
                Grr.of(grr),
                passwordHasher.hash(SENHA),
                senhaAlterada,
                lgpd,
                if (lgpd == null) null else "127.0.0.1",
                if (lgpd == null) null else "it",
                true,
                0,
                null,
                authorities,
                agora,
                agora,
            ),
        )
    }

    private fun login(email: String): String {
        val result = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identificador\":\"$email\",\"senha\":\"$SENHA\"}"),
        )
            .andExpect(status().isOk)
            .andReturn()
        return extract(result.response.contentAsString, "\"accessToken\":\"", "\"")
    }

    companion object {
        private const val SENHA = "TroqueEstaSenha1!"
        private const val PIN = "123456"
        private const val EMAIL_ALUNO = "it.formativa@ufpr.br"
        private const val EMAIL_PROFESSOR = "it.formativa.prof@ufpr.br"
        private const val CODIGO = "TADS-FORM-IT"
        private const val DEVICE = "44444444-4444-4444-8444-444444444444"
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
