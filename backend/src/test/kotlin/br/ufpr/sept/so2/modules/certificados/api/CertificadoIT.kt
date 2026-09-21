package br.ufpr.sept.so2.modules.certificados.api

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
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.hamcrest.Matchers.startsWith
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CertificadoIT {

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
        usuariosIt.criarUsuario(EMAIL_ALUNO, "GRR20249901", AUTHORITIES_ALUNO)
        criarAlunoSeAusente("Aluno Certificado IT", "GRR20249901", EMAIL_ALUNO, cursoId, agora)
        usuariosIt.criarUsuario(EMAIL_OUTRO, "GRR20249902", AUTHORITIES_ALUNO)
        criarAlunoSeAusente("Outro Aluno Cert IT", "GRR20249902", EMAIL_OUTRO, cursoId, agora)
        usuariosIt.criarUsuario(EMAIL_SEM_CAP, "GRR20249903", AUTHORITIES_SEM_CERT)
        criarAlunoSeAusente("Aluno Sem Cert Cap", "GRR20249903", EMAIL_SEM_CAP, cursoId, agora)
        usuariosIt.criarUsuario(EMAIL_CAAF, "GRR20249904", listOf("dashboard.view_own", "formative.review"))
    }

    @Test
    fun caafAprovaEmiteCertificadoDownloadEVerificacaoPublica() {
        val aberto = salvarEvento()
        val tokenAluno = usuariosIt.login(EMAIL_ALUNO)

        mockMvc.perform(
            post("/events/${aberto.id}/attendance/confirm")
                .header("Authorization", "Bearer $tokenAluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(confirmBody()),
        )
            .andExpect(status().isOk)

        val listaFormativa = mockMvc.perform(
            get("/formativas")
                .param("audience", "me")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
        val formativaId = extract(listaFormativa, "\"id\":\"", "\"")

        mockMvc.perform(
            post("/formativas/$formativaId/confirmar")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)

        val tokenCaaf = usuariosIt.login(EMAIL_CAAF)
        mockMvc.perform(
            post("/formativas/$formativaId/aprovar")
                .header("Authorization", "Bearer $tokenCaaf")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"parecer":"Presença validada; emite certificado oficial."}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("APROVADA"))

        mockMvc.perform(
            post("/formativas/$formativaId/aprovar")
                .header("Authorization", "Bearer $tokenCaaf")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"parecer":"Reaprovar não deve duplicar certificado."}"""),
        )
            .andExpect(status().isConflict)

        val lista = mockMvc.perform(
            get("/certificates")
                .param("beneficiario", "me")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0]._links.download").value(startsWith("/certificates/")))
            .andExpect(jsonPath("$.content[0].hashSha256").isString)
            .andReturn()
            .response
            .contentAsString
        val certificadoId = extract(lista, "\"id\":\"", "\"")
        val hash = extract(lista, "\"hashSha256\":\"", "\"")
        val downloadHref = extract(lista, "\"download\":\"", "\"")

        mockMvc.perform(
            get(downloadHref)
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(header().string("Content-Disposition", startsWith("attachment;")))
            .andExpect { result ->
                val body = result.response.contentAsByteArray
                assert(body.size > 100)
                assert(body.copyOfRange(0, 4).contentEquals("%PDF".toByteArray()))
            }

        mockMvc.perform(get("/publico/certificados/$hash/verificacao"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("VALIDO"))
            .andExpect(jsonPath("$.hashSha256").value(hash))
            .andExpect(jsonPath("$.assinaturaEd25519").isString)
            .andExpect(jsonPath("$.beneficiarioNome").value("Aluno Certificado IT"))
            .andExpect(jsonPath("$.atividade").value(aberto.titulo))
            .andExpect(jsonPath("$.cargaHoraria").value(aberto.cargaHoraria))
            .andExpect(jsonPath("$.jwksUrl").value("/.well-known/jwks.json"))
            .andExpect(jsonPath("$.grr").doesNotExist())
            .andExpect(jsonPath("$.email").doesNotExist())

        mockMvc.perform(get("/.well-known/jwks.json"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.keys[0].kty").value("OKP"))
            .andExpect(jsonPath("$.keys[0].crv").value("Ed25519"))
            .andExpect(jsonPath("$.keys[0].x").isString)

        mockMvc.perform(get("/publico/certificados/${"f".repeat(64)}/verificacao"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.beneficiarioNome").doesNotExist())
            .andExpect(jsonPath("$.title").exists())

        val tokenSemCap = usuariosIt.login(EMAIL_SEM_CAP)
        mockMvc.perform(
            get("/certificates")
                .param("beneficiario", "me")
                .header("Authorization", "Bearer $tokenSemCap"),
        )
            .andExpect(status().isForbidden)

        val tokenOutro = usuariosIt.login(EMAIL_OUTRO)
        mockMvc.perform(
            get("/certificates/$certificadoId")
                .header("Authorization", "Bearer $tokenOutro"),
        )
            .andExpect(status().isNotFound)

        mockMvc.perform(
            get("/certificates/$certificadoId/download")
                .header("Authorization", "Bearer $tokenOutro"),
        )
            .andExpect(status().isNotFound)

        mockMvc.perform(
            get("/bff/dashboard/aluno")
                .header("Authorization", "Bearer $tokenAluno"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.kpis.certificados").value(1))
            .andExpect(jsonPath("$.kpis.certificados", not(nullValue())))
    }

    private fun garantirCurso(agora: OffsetDateTime): UUID {
        val existente = cursoRepository.findByCodigo(CODIGO)
        if (existente.isPresent) {
            return existente.get().id
        }
        return cursoRepository.save(
            Curso(Uuids.v7(), "TADS IT Certificado", "TADSC", CODIGO, null, 120, true, agora, agora),
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
                "IT certificado ${Uuids.v7()}",
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
        private const val EMAIL_ALUNO = "it.cert.aluno@ufpr.br"
        private const val EMAIL_OUTRO = "it.cert.outro@ufpr.br"
        private const val EMAIL_SEM_CAP = "it.cert.semcap@ufpr.br"
        private const val EMAIL_CAAF = "it.cert.caaf@ufpr.br"
        private const val CODIGO = "TADS-CERT-IT"
        private const val DEVICE = "55555555-5555-5555-8555-555555555556"
        private val AUTHORITIES_ALUNO = listOf(
            "dashboard.view_own",
            "attendance.view_open",
            "attendance.check_in",
            "formative.view_own",
            "formative.confirm_own",
            "certificate.view_own",
        )
        private val AUTHORITIES_SEM_CERT = listOf(
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
