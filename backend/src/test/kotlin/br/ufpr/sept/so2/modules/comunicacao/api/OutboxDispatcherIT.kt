package br.ufpr.sept.so2.modules.comunicacao.api

import br.ufpr.sept.so2.modules.comunicacao.application.DespacharOutboxUseCase
import br.ufpr.sept.so2.modules.comunicacao.infrastructure.RecordingMailAdapter
import br.ufpr.sept.so2.modules.iam.application.ports.JtiBlacklistRepository
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.domain.Usuario
import br.ufpr.sept.so2.modules.iam.infrastructure.persistence.OutboxEventJpaRepository
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository
import br.ufpr.sept.so2.modules.solicitacoes.infrastructure.DeclaracaoSimplesSeed
import br.ufpr.sept.so2.shared.domain.valueobject.Email
import br.ufpr.sept.so2.shared.domain.valueobject.Grr
import br.ufpr.sept.so2.shared.infrastructure.Uuids
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(OutboxDispatcherIT.MailConfig::class)
class OutboxDispatcherIT {

    @TestConfiguration
    class MailConfig {
        @Bean
        @Primary
        fun mailPort(): RecordingMailAdapter = RecordingMailAdapter()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordHasher: PasswordHasher

    @Autowired
    private lateinit var tipoSolicitacaoRepository: TipoSolicitacaoRepository

    @Autowired
    private lateinit var outboxEventJpaRepository: OutboxEventJpaRepository

    @Autowired
    private lateinit var despacharOutboxUseCase: DespacharOutboxUseCase

    @Autowired
    private lateinit var mailPort: RecordingMailAdapter

    @Autowired
    private lateinit var jwtTokenService: JwtTokenService

    @Autowired
    private lateinit var jtiBlacklistRepository: JtiBlacklistRepository

    @BeforeEach
    fun seed() {
        mailPort.limpar()
        val agora = OffsetDateTime.now()
        if (tipoSolicitacaoRepository.findByCodigo(DeclaracaoSimplesSeed.CODIGO).isEmpty) {
            tipoSolicitacaoRepository.save(DeclaracaoSimplesSeed.tipo(agora))
        }
        criarUsuario(
            EMAIL_RESET,
            "GRR20247701",
            listOf("dashboard.view_own"),
            agora,
        )
        criarUsuario(
            EMAIL_ALUNO,
            "GRR20247702",
            listOf("dashboard.view_own", "request.view_own", "request.open"),
            agora,
        )
        criarUsuario(
            EMAIL_PROFESSOR,
            "GRR20247703",
            listOf("dashboard.view_own", "event.manage", "event.host", "request.deliberate"),
            agora,
        )
        criarUsuario(
            EMAIL_SECRETARIA,
            "GRR20247704",
            listOf("dashboard.view_own", "request.deliberate", "request.triage"),
            agora,
        )
    }

    @Test
    fun recuperarSenhaDespachaSmtpENaoReenvia() {
        mockMvc.perform(
            post("/auth/recuperar-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$EMAIL_RESET"}"""),
        )
            .andExpect(status().isAccepted)

        val resetEvents = outboxEventJpaRepository.findAll().filter { it.tipo == "PASSWORD_RESET" }
        assertTrue(resetEvents.any { it.status == "PENDING" })

        despacharOutboxUseCase.execute()

        val enviados = mailPort.mensagens.filter { it.to == EMAIL_RESET }
        assertTrue(enviados.isNotEmpty())
        assertTrue(enviados.any { it.body.contains("/nova-senha?token=") })
        assertTrue(
            outboxEventJpaRepository.findAll().any { it.tipo == "PASSWORD_RESET" && it.status == "SENT" },
        )

        val nResetMail = enviados.size
        despacharOutboxUseCase.execute()
        assertEquals(nResetMail, mailPort.mensagens.filter { it.to == EMAIL_RESET }.size)

        val nReset = outboxEventJpaRepository.findAll().count { it.tipo == "PASSWORD_RESET" }
        mockMvc.perform(
            post("/auth/recuperar-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"naoexiste.outbox@ufpr.br"}"""),
        )
            .andExpect(status().isAccepted)
        assertEquals(nReset, outboxEventJpaRepository.findAll().count { it.tipo == "PASSWORD_RESET" })
        assertEquals(nResetMail, mailPort.mensagens.filter { it.to == EMAIL_RESET }.size)
        assertTrue(mailPort.mensagens.none { it.to.contains("naoexiste") })
    }

    @Test
    fun smtpFalhaNaoDesfazAMutacaoEVoltaPending() {
        mockMvc.perform(
            post("/auth/recuperar-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$EMAIL_RESET"}"""),
        )
            .andExpect(status().isAccepted)
        val pendente = outboxEventJpaRepository.findAll()
            .filter { it.tipo == "PASSWORD_RESET" && it.status == "PENDING" }
            .maxByOrNull { it.createdAt!! }!!
        mailPort.falhar = true

        despacharOutboxUseCase.execute()

        val depois = outboxEventJpaRepository.findById(pendente.id!!).get()
        assertEquals("PENDING", depois.status)
        assertEquals(1, depois.tentativas)
        assertTrue(usuarioRepository.findByEmail(EMAIL_RESET).isPresent)
        assertTrue(mailPort.mensagens.none { it.to == EMAIL_RESET })
    }

    @Test
    fun alunoCriaSolicitacaoProfessorDeliberaPorDeepLinkEFilaSemToken() {
        val tokenAluno = login(EMAIL_ALUNO)
        val created = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $tokenAluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadNova("Deep-link de deliberação")),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val id = extract(created.response.contentAsString, "\"id\":\"", "\"")

        despacharOutboxUseCase.execute()

        val mailsProf = mailPort.mensagens.filter {
            it.to == EMAIL_PROFESSOR && it.body.contains("/solicitacoes/$id/deliberar?token=")
        }
        assertEquals(1, mailsProf.size)
        assertTrue(mailsProf.first().body.contains("/solicitacoes/$id/deliberar?token="))
        assertTrue(mailPort.mensagens.none { it.to == EMAIL_SECRETARIA })

        val jwt = tokenDoCorpo(mailsProf.first().body)
        val tokenProfessor = login(EMAIL_PROFESSOR)

        mockMvc.perform(
            get("/requests/$id")
                .param("token", jwt)
                .header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.deferir").exists())

        mockMvc.perform(
            get("/requests/$id")
                .param("token", jwt)
                .header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isOk)

        mockMvc.perform(
            post("/requests/$id/transitions")
                .param("token", jwt)
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"action":"DEFER","parecer":"Deferido via deep-link de 72h."}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("DELIBERADA"))

        val claims = jwtTokenService.parseDeliberationToken(jwt)
        assertTrue(jtiBlacklistRepository.contains(claims.jti))

        mockMvc.perform(
            post("/requests/$id/transitions")
                .param("token", jwt)
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"action":"DEFER","parecer":"Segundo uso do mesmo link."}"""),
        )
            .andExpect(status().isUnauthorized)

        val created2 = mockMvc.perform(
            post("/requests")
                .header("Authorization", "Bearer $tokenAluno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadNova("Fila sem token")),
        )
            .andExpect(status().isCreated)
            .andReturn()
        val id2 = extract(created2.response.contentAsString, "\"id\":\"", "\"")

        mockMvc.perform(
            get("/requests")
                .param("canDeliberate", "true")
                .header("Authorization", "Bearer $tokenProfessor"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[*].id", hasItem(id2)))

        mockMvc.perform(
            post("/requests/$id2/transitions")
                .header("Authorization", "Bearer $tokenProfessor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"action":"DEFER","parecer":"Deferido pela fila, sem deep-link."}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("DELIBERADA"))
    }

    private fun criarUsuario(email: String, grr: String, authorities: List<String>, agora: OffsetDateTime) {
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
                true,
                agora,
                "127.0.0.1",
                "it",
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
        private const val EMAIL_RESET = "it.outbox.reset@ufpr.br"
        private const val EMAIL_ALUNO = "it.outbox.aluno@ufpr.br"
        private const val EMAIL_PROFESSOR = "it.outbox.prof@ufpr.br"
        private const val EMAIL_SECRETARIA = "it.outbox.sec@ufpr.br"

        private fun payloadNova(finalidade: String): String =
            """
            {
              "tipoCodigo": "DECLARACAO_SIMPLES",
              "payload": {
                "finalidade": "$finalidade",
                "observacao": "Outbox dispatcher"
              }
            }
            """.trimIndent()

        private fun extract(json: String, startToken: String, endToken: String): String {
            val start = json.indexOf(startToken)
            val from = start + startToken.length
            val end = json.indexOf(endToken, from)
            return json.substring(from, end)
        }

        private fun tokenDoCorpo(body: String): String {
            val marker = "deliberar?token="
            val start = body.indexOf(marker)
            require(start >= 0) { "deep-link ausente no e-mail" }
            val from = start + marker.length
            val end = body.indexOfAny(charArrayOf(' ', '\n', '\r'), from).let { if (it < 0) body.length else it }
            return body.substring(from, end).trim()
        }
    }
}
